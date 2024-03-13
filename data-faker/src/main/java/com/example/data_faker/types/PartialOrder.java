package com.example.data_faker.types;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PartialOrder {
    private Integer orderParts;
    private List<ProductVolume> products;

    public void addProduct(ProductVolume product) {
        this.products.add(product);
    }

    public boolean containsUnallocatedOrder() {
        return this.getProducts()
            .stream()
            .anyMatch((p) -> p.hasNoVolume());
    }

    public boolean hasAllProducts() {
        return this.getSubOrderCount() == this.getOrderParts();
    }

    public Integer getSubOrderCount() {
        return this.products.size();
    }

    public Order intoOrder(OrderState status) {
        return Order.builder()
            .products(this.getProducts())
            .status(status)
            .build();
    }
}
