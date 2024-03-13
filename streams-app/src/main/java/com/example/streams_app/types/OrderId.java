package com.example.streams_app.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @NoArgsConstructor @AllArgsConstructor @Setter
public class OrderId {
    @JsonProperty
    private Long orderId;
}
