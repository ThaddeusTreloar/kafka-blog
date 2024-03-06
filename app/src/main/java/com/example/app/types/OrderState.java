package com.example.app.types;

public enum OrderState {
    PENDING,
    ALLOCATED,
    REJECTED;

    public boolean is(OrderState rhs) {
        return this == rhs;
    }

    public boolean isNot(OrderState rhs) {
        return this != rhs;
    }
}
