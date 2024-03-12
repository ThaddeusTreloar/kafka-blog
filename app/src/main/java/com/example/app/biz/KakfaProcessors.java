package com.example.app.biz;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.app.types.LogisticsOrder;
import com.example.app.types.OrderState;
import com.example.app.util.Topics;

@Component
public class KakfaProcessors {
    @Autowired
    public void join_processed_orders(StreamsBuilder builder, Topics kafkaTopicsConfig) {
        var customers_topics = kafkaTopicsConfig.getCustomers();
        var orders_topic = kafkaTopicsConfig.getOrders();
        var logistics_order_topic = kafkaTopicsConfig.getLogisticsOrder();

        var customers = builder.globalTable(
            customers_topics.getName(),
            Consumed.with(
                customers_topics.getKeySerde(),
                customers_topics.getValueSerde()
            )
        );

        var orders = builder.stream(
            orders_topic.getName(),
            Consumed.with(
                orders_topic.getKeySerde(),
                orders_topic.getValueSerde()
            )
        );

        orders.filter(
            (k, v) -> v.is(OrderState.ALLOCATED)
        ).join(
            customers,
            (k, v) -> v.getCustomerId(),
            (order, customer) -> LogisticsOrder.builder()
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .address(customer.getAddress())
                .city(customer.getCity())
                .country(customer.getCountry())
                .state(customer.getState())
                .zip(customer.getZip())
                .status(order.getStatus())
                .products(order.getProducts())
                .build()
        ).to(
            logistics_order_topic.getName(),
            Produced.with(
                logistics_order_topic.getKeySerde(), 
                logistics_order_topic.getValueSerde()
            )
        );
    }
}