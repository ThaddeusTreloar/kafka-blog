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

import com.example.app.util.Topics;

import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde;
import lombok.extern.java.Log;
import scala.collection.mutable.StringBuilder;

@Log
@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {
    public static String SPRING_KAFKA_API_KEY_ENV = "SPRING_KAFKA_API_KEY";
    public static String SPRING_KAFKA_API_SECRET_ENV = "SPRING_KAFKA_API_SECRET";
    public static String SPRING_KAFKA_BOOTSTRAP_SERVERS_ENV = "SPRING_KAFKA_BOOTSTRAP_SERVERS";
    public static String SPRING_KAFKA_SCHEMA_REGISTRY_URL_ENV = "SPRING_KAFKA_SCHEMA_REGISTRY_URL";
    public static String SPRING_KAFKA_SCHEMA_USER_ENV = "SPRING_KAFKA_SCHEMA_USER";
    public static String SPRING_KAFKA_SCHEMA_PASS_ENV = "SPRING_KAFKA_SCHEMA_PASS";

    @Autowired
    private Environment environment;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfigs(KafkaEnv kafkaEnvConfig) {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "springApplication");
 
        props.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
    
        if (kafkaEnvConfig.isNull()) {
            throw new MissingVarException(kafkaEnvConfig.getNullVar());
        }

        var sasl_jaas_config = new StringBuilder()
            .append("org.apache.kafka.common.security.plain.PlainLoginModule required username='")
            .append(kafkaEnvConfig.getApiKey())
            .append("' password='")
            .append(kafkaEnvConfig.getApiSecret())
            .append("';")
            .toString();

        props.put(SaslConfigs.SASL_JAAS_CONFIG, sasl_jaas_config);

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaEnvConfig.getBootstrapServers());

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, KafkaJsonSchemaSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaJsonSchemaSerde.class.getName());

        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());

        log.info("Finished kafka config");

        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KafkaEnv kafkaEnvConfig() {
        var config = KafkaEnv.builder()
            .apiKey(environment.getProperty(SPRING_KAFKA_API_KEY_ENV))
            .apiSecret(environment.getProperty(SPRING_KAFKA_API_SECRET_ENV))
            .bootstrapServers(environment.getProperty(SPRING_KAFKA_BOOTSTRAP_SERVERS_ENV))
            .schemaRegistryUrl(environment.getProperty(SPRING_KAFKA_SCHEMA_REGISTRY_URL_ENV))
            .schemaRegistryUser(environment.getProperty(SPRING_KAFKA_SCHEMA_USER_ENV))
            .schemaRegistryPass(environment.getProperty(SPRING_KAFKA_SCHEMA_PASS_ENV))
            .build();

        return config;
    }

    @Bean
    public Topics kafkaTopicsConfig(KafkaEnv kafkaEnvConfig) {
        return new Topics(kafkaEnvConfig);
    }
}