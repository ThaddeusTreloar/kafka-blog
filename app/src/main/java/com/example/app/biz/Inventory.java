package com.example.app.biz;

import java.util.HashMap;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import com.example.app.types.JoinedSubOrder;
import com.example.app.types.Order;
import com.example.app.types.OrderState;
import com.example.app.util.Topics;

import lombok.extern.java.Log;

@Log
@Configuration
public class Inventory {

    public static String RESERVED_STOCK_STORE_NAME = "reserved-stock";
    public static String SUB_ORDERS_TOPIC = "sub-orders";
    public static String SUB_ORDER_COUNT_TOPIC = "sub-orders-count";

    
    @Bean
    public FactoryBean<StreamsBuilder> init_builder(KafkaStreamsConfiguration config) {
        var factory_bean = new StreamsBuilderFactoryBean(config);

        factory_bean.setAutoStartup(true);

        return factory_bean;
    }

    @Bean
    public void orders(StreamsBuilder builder) {
        var topics = Topics.build_topics();
        var orders_topic = topics.getOrders();
        var sub_order_validations_topic = topics.getSubOrderValidations();

        var orders = builder
            .stream(
                orders_topic.getName(),
                Consumed.with(orders_topic.getKeySerde(), orders_topic.getValueSerde())
            );

        orders.filter((id, order) -> order.is(OrderState.PENDING))
            .flatMap((id, order) -> order.intoSubOrders(id))
            .to(sub_order_validations_topic.getName());

        builder.stream(
                sub_order_validations_topic.getName(),
                Consumed.with(sub_order_validations_topic.getKeySerde(), sub_order_validations_topic.getValueSerde())
            ).groupByKey()
            .aggregate(() -> Order.builder().status(OrderState.PENDING).build(), (k, v, agg) -> {
                agg.addProduct(v.intoOrderProduct());

                if (!v.isFullfilled()) {
                    agg.setStatus(OrderState.REJECTED);
                } else if (v.getSubOrder().getOrderParts() == agg.getSubOrderCount() && agg.isNot(OrderState.REJECTED)) {
                    agg.setStatus(OrderState.ALLOCATED);
                }

                return agg;
            }).toStream()
            .filter(
                (k, v) -> v.isNot(OrderState.PENDING)
            ).to(orders_topic.getName());
    }

    @Bean void current_inventory(StreamsBuilder builder) {
        var topics = Topics.build_topics();

        var inventory_topic = topics.getInventory();
        var sub_order_validations_topic = topics.getSubOrderValidations();
        var sub_orders_topic = topics.getSubOrders();
        var net_inventory_topic = topics.getNetInventory();

        var inventory = builder.stream(
                inventory_topic.getName(),
                Consumed.with(inventory_topic.getKeySerde(), inventory_topic.getValueSerde())
            ).toTable();

        var sub_orders = builder.stream(
                sub_orders_topic.getName(),
                Consumed.with(sub_orders_topic.getKeySerde(), sub_orders_topic.getValueSerde())
            );

        var reservedStock = Stores
            .keyValueStoreBuilder(
                Stores.persistentKeyValueStore(net_inventory_topic.getName()),
                net_inventory_topic.getKeySerde(), net_inventory_topic.getValueSerde()
            )
            .withLoggingEnabled(new HashMap<>());

        builder.addStateStore(reservedStock);

        sub_orders
            .join(
                inventory, 
                (left, right) -> JoinedSubOrder.builder()
                    .subOrder(left)
                    .currentInventory(right)
                    .build()
            ).process(OrderValidator::new, net_inventory_topic.getName())
            .to(sub_order_validations_topic.getName());
    }
}
