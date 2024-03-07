package com.example.app.biz;

import java.util.HashMap;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.app.types.JoinedSubOrder;
import com.example.app.types.Order;
import com.example.app.types.OrderState;
import com.example.app.util.Topics;

@Component
public class KakfaProcessors {
    @Autowired
    public void aggregte_sub_orders(StreamsBuilder builder) {
        var topics = Topics.build_topics();
        var inventory_topic = topics.getWarehouseInventory();
        var orders_topic = topics.getOrders();
        var sub_order_validations_topic = topics.getSubOrderValidations();

        var validated_orders = builder.stream(
                sub_order_validations_topic.getName(),
                Consumed.with(sub_order_validations_topic.getKeySerde(), sub_order_validations_topic.getValueSerde())
            ).groupByKey()
            .aggregate(Order::newPending, (k, validate_sub_order, joined_order) -> {
                joined_order.setOrderId(k);
                joined_order.addProduct(validate_sub_order.intoOrderProduct(k));
                
                if (
                    validate_sub_order.getOrderParts() == joined_order.getSubOrderCount() 
                ) {
                    if (joined_order.containsUnallocatedOrder()) {
                        joined_order.setStatus(OrderState.REJECTED);
                    } else {
                        joined_order.setStatus(OrderState.ALLOCATED);
                    }
                }

                return joined_order;
            }).toStream();

        // Confirm allocated orders
        validated_orders.filter((k, v) -> v.is(OrderState.ALLOCATED))
            .to(
                orders_topic.getName(),
                Produced.with(
                    orders_topic.getKeySerde(),
                    orders_topic.getValueSerde()
                )
            );

        // Return all allocated stock for rejected order to warehouse inventory
        validated_orders.filter((k, v) -> v.is(OrderState.REJECTED))
            .flatMapValues(
                (k, v) -> v.getProducts()
            ).filter(
                (k, v) -> v.hasNoVolume()   
            ).map(
                (k, v) -> new KeyValue<>(v.getId(), v.getVolume())
            ).to(
                inventory_topic.getName(),
                Produced.with(
                    inventory_topic.getKeySerde(), 
                    inventory_topic.getValueSerde()
                )
            );
    }

    @Autowired
    public void split_orders(StreamsBuilder builder) {
        var topics = Topics.build_topics();
        var orders_topic = topics.getOrders();
        var sub_orders_topic = topics.getSubOrders();

        // Split order into product sub_orders
        builder
            .stream(
                orders_topic.getName(),
                Consumed.with(orders_topic.getKeySerde(), orders_topic.getValueSerde())
            ).filter((id, order) -> order.is(OrderState.PENDING))
            .flatMap((id, order) -> order.intoSubOrders(id))
            .to(
                sub_orders_topic.getName(),
                Produced.with(
                    sub_orders_topic.getKeySerde(),
                    sub_orders_topic.getValueSerde()
                )
            );
    }

    @Autowired 
    public void validate_sub_orders(StreamsBuilder builder) {
        var topics = Topics.build_topics();

        var inventory_topic = topics.getWarehouseInventory();
        var sub_order_validations_topic = topics.getSubOrderValidations();
        var sub_orders_topic = topics.getSubOrders();
        var allocated_inventory_topic = topics.getAllocatedInventory();

        var inventory = builder.stream(
                inventory_topic.getName(),
                Consumed.with(inventory_topic.getKeySerde(), inventory_topic.getValueSerde())
            ).toTable();

        var sub_orders = builder.stream(
                sub_orders_topic.getName(),
                Consumed.with(sub_orders_topic.getKeySerde(), sub_orders_topic.getValueSerde())
            );

        var allocated_inventory = Stores
            .keyValueStoreBuilder(
                Stores.persistentKeyValueStore(allocated_inventory_topic.getName()),
                allocated_inventory_topic.getKeySerde(), allocated_inventory_topic.getValueSerde()
            )
            .withLoggingEnabled(new HashMap<>());

        builder.addStateStore(allocated_inventory);

        sub_orders
            .join(
                inventory, 
                (left, right) -> JoinedSubOrder.builder()
                    .subOrder(left)
                    .currentInventory(right)
                    .build()
            ).process(OrderValidator::new, allocated_inventory_topic.getName())
            .to(
                sub_order_validations_topic.getName(),
                Produced.with(
                    sub_order_validations_topic.getKeySerde(),
                    sub_order_validations_topic.getValueSerde()
                )
            );
    }
}