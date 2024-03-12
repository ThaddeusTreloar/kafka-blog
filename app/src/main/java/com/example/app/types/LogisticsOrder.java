package com.example.app.types;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder @Getter @AllArgsConstructor @NoArgsConstructor
public class LogisticsOrder {
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private String address;
    @JsonProperty
    private String city;
    @JsonProperty
    private String country;
    @JsonProperty
    private String state;
    @JsonProperty
    private String zip;
    @JsonProperty
    private OrderState status;
    @JsonProperty
    private List<ProductVolume> products;
}
