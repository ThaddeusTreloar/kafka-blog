package com.example.app.types;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder @Getter @Setter
public class SubOrder {
    private Long orderId;
    private Integer orderParts;
    private Long volume;

    public SubOrder withVolume(Long new_volume) {
        this.setVolume(new_volume);

        return this;
    }

    public ValidatedSubOrder intoValidatedSubOrder(Long product_id) {
        return ValidatedSubOrder.builder()
            .productId(product_id)
            .orderParts(this.getOrderParts())
            .volume(this.getVolume())
            .build();
    }
}
