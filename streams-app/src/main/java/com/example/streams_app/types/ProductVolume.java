package com.example.streams_app.types;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder @Getter @Setter
public class ProductVolume {
    private Long productId;
    private Long volume;

    public boolean hasNoVolume() {
        return this.getVolume() == 0;
    }

    public ProductVolume addVolume(ProductVolume other) {
        this.setVolume(this.getVolume() + other.getVolume());

        return this;
    }

    public ProductVolume addVolume(Long other) {
        this.setVolume(this.getVolume() + other);

        return this;
    }

    public InventoryVolume intoInventoryVolume() {
        return new InventoryVolume(this.getVolume());
    }

    public ProductId intoProductId() {
        return new ProductId(this.getProductId());
    }
}
