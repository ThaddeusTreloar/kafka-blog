package com.example.streams_app.types;

public enum FulfillmentStatus {
    FULFILLED,
    UNFULFILLED;

    public boolean is(FulfillmentStatus rhs) {
        return this == rhs;
    }
}
