package com.example.app.util;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.example.app.types.Order;
import com.example.app.types.SubOrder;
import com.example.app.types.ValidatedSubOrder;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class Topics {
    private Topic<Long, Long> warehouseInventory;
    private Topic<Long, Long> allocatedInventory;
    private Topic<Long, Order> orders;
    private Topic<Long, SubOrder> subOrders;
    private Topic<Long, ValidatedSubOrder> subOrderValidations;

    private static String WAREHOUSE_INVENTORY_TOPIC = "INVENTORY";
    private static String ALLOCATED_INVENTORY_TOPIC = "NET_INVENTORY";
    private static String ORDERS_TOPIC = "ORDERS";
    private static String SUB_ORDERS_TOPIC = "SUB_ORDERS";
    private static String SUB_ORDER_VALIDATIONS_TOPIC = "SUB_ORDER_VALIDATIONS";

    public static Topics build_topics() {
        var warehouse_inventory = Topic.<Long, Long>builder()
            .name(WAREHOUSE_INVENTORY_TOPIC)
            .keySerde(Serdes.Long())
            .valueSerde(Serdes.Long())
            .build();

        var allocated_inventory = Topic.<Long, Long>builder()
            .name(ALLOCATED_INVENTORY_TOPIC)
            .keySerde(Serdes.Long())
            .valueSerde(Serdes.Long())
            .build();

        var orders = Topic.<Long, Order>builder()
            .name(ORDERS_TOPIC)
            .keySerde(Serdes.Long())
            .valueSerde(new JsonSerde<Order>())
            .build();

        var sub_orders = Topic.<Long, SubOrder>builder()
            .name(SUB_ORDERS_TOPIC)
            .keySerde(Serdes.Long())
            .valueSerde(new JsonSerde<SubOrder>())
            .build();

        var sub_order_validations = Topic.<Long, ValidatedSubOrder>builder()
            .name(SUB_ORDER_VALIDATIONS_TOPIC)
            .keySerde(Serdes.Long())
            .valueSerde(new JsonSerde<ValidatedSubOrder>())
            .build();

        return Topics.builder()
            .warehouseInventory(warehouse_inventory)
            .allocatedInventory(allocated_inventory)
            .orders(orders)
            .subOrders(sub_orders)
            .subOrderValidations(sub_order_validations)
            .build();
    }
}