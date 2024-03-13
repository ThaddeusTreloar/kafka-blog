package com.example.data_faker.biz;

import java.util.Properties;

import org.apache.flink.avro.generated.Order;
import org.apache.flink.avro.generated.OrderId;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @Builder
public class KafkaEnv {
    private String apiKey;
    private String apiSecret;
    private String bootstrapServers;
    private String schemaRegistryUrl;
    private String schemaRegistryUser;
    private String schemaRegistryPass;

    public static String SPRING_KAFKA_API_KEY_ENV = "SPRING_KAFKA_API_KEY";
    public static String SPRING_KAFKA_API_SECRET_ENV = "SPRING_KAFKA_API_SECRET";
    public static String SPRING_KAFKA_BOOTSTRAP_SERVERS_ENV = "SPRING_KAFKA_BOOTSTRAP_SERVERS";
    public static String SPRING_KAFKA_SCHEMA_REGISTRY_URL_ENV = "SPRING_KAFKA_SCHEMA_REGISTRY_URL";
    public static String SPRING_KAFKA_SCHEMA_USER_ENV = "SPRING_KAFKA_SCHEMA_USER";
    public static String SPRING_KAFKA_SCHEMA_PASS_ENV = "SPRING_KAFKA_SCHEMA_PASS";

    public KafkaEnv() {
        this.apiKey =System.getenv(SPRING_KAFKA_API_KEY_ENV);
        this.apiSecret =System.getenv(SPRING_KAFKA_API_SECRET_ENV);
        this.bootstrapServers =System.getenv(SPRING_KAFKA_BOOTSTRAP_SERVERS_ENV);
        this.schemaRegistryUrl =System.getenv(SPRING_KAFKA_SCHEMA_REGISTRY_URL_ENV);
        this.schemaRegistryUser =System.getenv(SPRING_KAFKA_SCHEMA_USER_ENV);
        this.schemaRegistryPass =System.getenv(SPRING_KAFKA_SCHEMA_PASS_ENV);
    }

    public boolean isNull() {
        var r  = new OrderId();
        return this.getApiKey() == null  
            | this.getApiSecret() == null
            | this.getBootstrapServers() == null
            | this.getSchemaRegistryUrl() == null
            | this.getSchemaRegistryUser() == null
            | this.getSchemaRegistryPass() == null;
    }

    public Properties intoConfigMap() {
        var map = new Properties();

        var sasl_jaas_config = new StringBuilder()
            .append("org.apache.kafka.common.security.plain.PlainLoginModule required username='")
            .append(this.getApiKey())
            .append("' password='")
            .append(this.getApiSecret())
            .append("';")
            .toString();

        map.put("client-id", "Fake fella");
        map.put("security.protocol", "SASL_SSL");
        map.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        map.put(SaslConfigs.SASL_JAAS_CONFIG, sasl_jaas_config);
        map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.getBootstrapServers());
        map.put("schema.registry.url", this.getSchemaRegistryUrl());
        map.put(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
        map.put(
            SchemaRegistryClientConfig.USER_INFO_CONFIG, 
            new StringBuilder()
                .append(this.getSchemaRegistryUser())
                .append(":")
                .append(this.getSchemaRegistryPass())
                .toString()
        );

        return map;
    }
}
