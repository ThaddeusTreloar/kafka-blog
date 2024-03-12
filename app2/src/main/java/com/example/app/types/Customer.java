package com.example.app.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class Customer {
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
}
