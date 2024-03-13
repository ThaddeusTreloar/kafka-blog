package com.example.streams_app.types;

import java.util.List;

import org.apache.flink.avro.generated.Order;
import org.apache.flink.avro.generated.ProductVolume;
import org.apache.kafka.streams.KeyValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PartialOrder {
    private Long customerId;
    private Integer orderParts;
    private List<ProductVolume> products;

    public void addProduct(ProductVolume product) {
        this.products.add(product);
    }

    public boolean hasAllProducts() {
        return this.getSubOrderCount() == this.getOrderParts();
    }

    public Integer getSubOrderCount() {
        return this.products.size();
    }

    public Order intoOrder(String status) {
        return Order.newBuilder()
            .setCustomerId(this.getCustomerId())
            .setProducts(this.getProducts())
            .setStatus(status)
            .build();
    }
}
