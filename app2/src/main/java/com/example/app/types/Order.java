package com.example.app.types;

import java.util.List;

import org.apache.kafka.streams.KeyValue;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Order {
    @JsonProperty
    private OrderState status;
    @JsonProperty
    private Long customerId;
    @JsonProperty
    private List<ProductVolume> products;
}
