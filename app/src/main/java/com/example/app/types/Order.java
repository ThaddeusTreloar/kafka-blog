package com.example.app.types;

import java.util.List;

import org.apache.kafka.streams.KeyValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Order {
    private OrderState status;
    private List<ProductVolume> products;

    public boolean is(OrderState state) {
        return this.status.is(state);
    }

    public boolean isNot(OrderState state) {
        return this.status.isNot(state);
    }

    public boolean containsUnallocatedOrder() {
        return this.getProducts()
            .stream()
            .anyMatch((p) -> p.hasNoVolume());
    }

    public List<KeyValue<ProductId, SubOrder>> intoSubOrders(Long order_id) {
        return this.products
            .stream()
            .map(
                (product) -> {
                    var product_id = new ProductId(product.getProductId());
                    var new_suborder = SubOrder.builder()
                        .orderId(order_id)
                        .volume(product.getVolume())
                        .orderParts(this.getProducts().size())
                        .build();

                    return new KeyValue<ProductId, SubOrder>(product_id, new_suborder);
                }
            ).toList();
    }
}
