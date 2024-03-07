package com.example.app.biz;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaEnv {
    private String apiKey;
    private String apiSecret;
    private String bootstrapServers;

    public boolean isNull() {
        return this.getApiKey() == null  
            | this.getApiSecret() == null
            | this.getBootstrapServers() == null;
    }

    public String getNullVar() {
        if (this.getApiKey() == null) {
            return "SPRING_KAFKA_API_KEY";
        }
        else if (this.getApiSecret() == null) {
            return "SPRING_KAFKA_API_SECRET";
        }
        else if (this.getBootstrapServers() == null) {
            return "SPRING_KAFKA_BOOTSTRAP_SERVERS";
        } else {
            return "";
        }
    } 
}
