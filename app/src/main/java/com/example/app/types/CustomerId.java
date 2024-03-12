package com.example.app.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @AllArgsConstructor @Setter @NoArgsConstructor
public class CustomerId {
    @JsonProperty
    private Long customerId;
}
