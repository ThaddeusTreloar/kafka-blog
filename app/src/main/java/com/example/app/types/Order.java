package com.example.app.types;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class Order {
    private Double orderId;
    private OrderState status;
    private List<Double> products;

    public boolean is(OrderState state) {
        return this.status.is(state);
    }
}
