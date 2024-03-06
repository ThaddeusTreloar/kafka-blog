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
    private Long orderId;
    private OrderState status;
    private List<OrderProduct> products;

    public void addProduct(OrderProduct product) {
        this.products.add(product);
    }

    public boolean is(OrderState state) {
        return this.status.is(state);
    }

    public boolean isNot(OrderState state) {
        return this.status.isNot(state);
    }

    public static Order newPending() {
        return Order.builder()
            .status(OrderState.PENDING)
            .build();
    }

    public List<KeyValue<Long, SubOrder>> intoSubOrders() {
        return this.products
            .stream()
            .map(
                (product) -> {
                    var product_id = product.getId();
                    var new_suborder = SubOrder.builder()
                        .orderId(this.getOrderId())
                        .volume(product.getVolume())
                        .orderParts(this.getProducts().size())
                        .build();

                    return new KeyValue<Long, SubOrder>(product_id, new_suborder);
                }
            ).toList();
    }

    public List<KeyValue<Long, SubOrder>> intoSubOrders(Long id) {
        return this.products
            .stream()
            .map(
                (product) -> {
                    var product_id = product.getId();
                    var new_suborder = SubOrder.builder()
                        .orderId(id)
                        .volume(product.getVolume())
                        .orderParts(this.getProducts().size())
                        .build();

                    return new KeyValue<Long, SubOrder>(product_id, new_suborder);
                }
            ).toList();
    }

    public Integer getSubOrderCount() {
        return this.products.size();
    }
}
