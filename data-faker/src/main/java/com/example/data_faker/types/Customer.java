package com.example.data_faker.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder @Getter @ToString
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
