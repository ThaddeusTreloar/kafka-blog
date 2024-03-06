package com.example.app.types;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class SubOrder {
    private Long orderId;
    private Integer orderParts;
    private Long volume;

    public OrderProduct inOrderProduct() {
        return OrderProduct.builder()
            .id(this.getOrderId())
            .volume(this.getVolume())
            .build();
    }
}
