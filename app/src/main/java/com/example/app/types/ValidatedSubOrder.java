package com.example.app.types;

import lombok.Builder;
import lombok.Getter;
@Builder @Getter
public class ValidatedSubOrder {
    private Long productId;
    private Integer orderParts;
    private Long allocatedVolume;

    public OrderProduct intoOrderProduct(Long product_id) {
        return OrderProduct.builder()
            .id(product_id)
            .volume(this.getAllocatedVolume())
            .build();
    }
}
