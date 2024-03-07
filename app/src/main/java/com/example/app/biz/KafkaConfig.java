package com.example.app.biz;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {

    public static String SPRING_KAFKA_API_KEY_ENV = "SPRING_KAFKA_API_KEY";
    public static String SPRING_KAFKA_API_SECRET_ENV = "SPRING_KAFKA_API_SECRET";
    public static String SPRING_KAFKA_BOOTSTRAP_SERVERS_ENV = "SPRING_KAFKA_BOOTSTRAP_SERVERS";

    @Autowired
    private Environment environment;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "springApplication");

        props.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");

        var kafka_config = new KafkaEnv(
            environment.getProperty(SPRING_KAFKA_API_KEY_ENV),
            environment.getProperty(SPRING_KAFKA_API_SECRET_ENV),
            environment.getProperty(SPRING_KAFKA_BOOTSTRAP_SERVERS_ENV)
        );

        if (kafka_config.isNull()) {
            throw new MissingVarException(kafka_config.getNullVar());
        }

        var sasl_jaas_config = new StringBuilder()
            .append("org.apache.kafka.common.security.plain.PlainLoginModule required username='")
            .append(kafka_config.getApiKey())
            .append("' password='")
            .append(kafka_config.getApiSecret())
            .append("';")
            .toString();

        props.put(SaslConfigs.SASL_JAAS_CONFIG, sasl_jaas_config);

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka_config.getBootstrapServers());

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());
        return new KafkaStreamsConfiguration(props);
    }
}
