package com.example.app.util;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.example.app.types.Order;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class Topics {
    private Topic<Double, String> orderValidations;
    private Topic<Double, Integer> inventory;
    private Topic<Double, Order> orders;

    public static Topics build_topics() {
        var confirmed_orders = Topic.<Double, String>builder()
            .name("confirmed_orders")
            .keySerde(Serdes.Double())
            .valueSerde(new JsonSerde<String>())
            .build();

        var inventory = Topic.<Double, Integer>builder()
            .name("confirmed_orders")
            .keySerde(Serdes.Double())
            .valueSerde(Serdes.Integer())
            .build();

        var orders = Topic.<Double, Order>builder()
            .name("confirmed_orders")
            .keySerde(Serdes.Double())
            .valueSerde(new JsonSerde<Order>())
            .build();

        return Topics.builder()
            .orderValidations(confirmed_orders)
            .inventory(inventory)
            .orders(orders)
            .build();
    }
}