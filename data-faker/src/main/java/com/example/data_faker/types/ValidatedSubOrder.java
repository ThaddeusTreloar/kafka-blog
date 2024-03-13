package com.example.data_faker.types;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder @Getter @Setter
public class ValidatedSubOrder {
    private Long productId;
    private Integer orderParts;
    private Long volume;

    public ValidatedSubOrder withVolume(Long new_volume) {
        this.setVolume(new_volume);

        return this;
    }

    public ProductVolume intoProductVolume() {
        return ProductVolume.builder()
            .productId(this.getProductId())
            .volume(this.getVolume())
            .build();
    }
}
