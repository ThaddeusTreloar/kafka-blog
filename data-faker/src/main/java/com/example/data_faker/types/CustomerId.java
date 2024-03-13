package com.example.data_faker.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @AllArgsConstructor @Setter @ToString
public class CustomerId {
    @JsonProperty
    private Long customerId;
}
