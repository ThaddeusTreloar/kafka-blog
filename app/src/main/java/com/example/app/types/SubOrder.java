package com.example.app.types;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class SubOrder {
    private Long id;
    private Integer orderParts;
    private Long volume;

    public OrderProduct inOrderProduct() {
        return OrderProduct.builder()
            .id(this.getId())
            .volume(this.getVolume())
            .build();
    }
}
