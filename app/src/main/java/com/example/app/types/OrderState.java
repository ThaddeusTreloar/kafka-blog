package com.example.app.types;

public enum OrderState {
    PENDING,
    CONFIRMED,
    REJECTED;

    public boolean is(OrderState cmp) {
        return this == cmp;
    }
}
