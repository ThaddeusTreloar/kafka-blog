package com.example.data_faker.types;

public enum FulfillmentStatus {
    FULFILLED,
    UNFULFILLED;

    public boolean is(FulfillmentStatus rhs) {
        return this == rhs;
    }
}
