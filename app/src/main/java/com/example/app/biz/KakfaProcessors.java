package com.example.app.biz;

import java.util.HashMap;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.app.types.JoinedSubOrder;
import com.example.app.types.Order;
import com.example.app.types.PartialOrder;
import com.example.app.types.OrderId;
import com.example.app.types.OrderState;
import com.example.app.util.Topics;

@Component
public class KakfaProcessors {
    @Autowired
    public void aggregte_sub_orders(StreamsBuilder builder, Topics kafkaTopicsConfig) {
var inventory_topic = kafkaTopicsConfig.getWarehouseInventory();
var orders_topic = kafkaTopicsConfig.getOrders();
var sub_order_validations_topic = kafkaTopicsConfig.getSubOrderValidations();

var validated_orders = builder.stream(
        sub_order_validations_topic.getName(),
        Consumed.with(sub_order_validations_topic.getKeySerde(), sub_order_validations_topic.getValueSerde())
    ).groupByKey()
    .aggregate(PartialOrder::new, (k, validate_sub_order, joined_order) -> {
        joined_order.addProduct(validate_sub_order.intoProductVolume());
        joined_order.setOrderParts(validate_sub_order.getOrderParts());
        
        return joined_order;
    }).toStream();

var agg_orders = validated_orders.filter((k, v) -> v.hasAllProducts());

agg_orders.filter((k, v) -> !v.containsUnallocatedOrder())
    .mapValues((k, v) -> v.intoOrder(OrderState.ALLOCATED))
    .to(
        orders_topic.getName()
    );

agg_orders.filter((k, v) -> v.containsUnallocatedOrder())
    .mapValues((k, v) -> v.intoOrder(OrderState.REJECTED))
    .to(
        orders_topic.getName()
    );

agg_orders.filter((k, v) -> v.containsUnallocatedOrder())
    .flatMapValues(
        (k, v) -> v.getProducts()
    ).filter(
        (k, v) -> v.hasNoVolume()
    ).to(
        inventory_topic.getName()
    );
    }

    @Autowired
    public void split_orders(StreamsBuilder builder, Topics kafkaTopicsConfig) {
        var orders_topic = kafkaTopicsConfig.getOrders();
        var sub_orders_topic = kafkaTopicsConfig.getSubOrders();

        // Split order into product sub_orders
        builder
            .<OrderId, Order>stream(
                orders_topic.getName(),
                Consumed.with(orders_topic.getKeySerde(), orders_topic.getValueSerde())
            ).filter((id, order) -> order.is(OrderState.PENDING))
            .flatMap((id, order) -> order.intoSubOrders(id.getOrderId()))
            .to(
                sub_orders_topic.getName()
            );
    }

    @Autowired 
    public void validate_sub_orders(StreamsBuilder builder, Topics kafkaTopicsConfig) {
        var inventory_topic = kafkaTopicsConfig.getWarehouseInventory();
        var sub_order_validations_topic = kafkaTopicsConfig.getSubOrderValidations();
        var sub_orders_topic = kafkaTopicsConfig.getSubOrders();
        var allocated_inventory_topic = kafkaTopicsConfig.getAllocatedInventory();

        var inventory = builder.stream(
                inventory_topic.getName(),
                Consumed.with(inventory_topic.getKeySerde(), inventory_topic.getValueSerde())
            ).groupByKey()
            .reduce((left, right) -> left.addVolume(right));

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
                    .currentInventory(right.getVolume())
                    .build()
            ).process(OrderValidator::new, allocated_inventory_topic.getName())
            .to(
                sub_order_validations_topic.getName()
            );
    }
}