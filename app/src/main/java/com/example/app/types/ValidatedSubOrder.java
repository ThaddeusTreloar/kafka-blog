package com.example.app.types;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class ValidatedSubOrder {
    private boolean isFullfilled;
    private SubOrder subOrder;

    public OrderProduct intoOrderProduct() {
        return this.getSubOrder().inOrderProduct();
    }
}
