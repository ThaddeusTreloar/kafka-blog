package com.example.data_faker.types;

import java.util.List;

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