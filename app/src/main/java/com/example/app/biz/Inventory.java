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
    public void aggregte_sub_orders(StreamsBuilder builder) {
        var topics = Topics.build_topics();
        var orders_topic = topics.getOrders();
        var sub_order_validations_topic = topics.getSubOrderValidations();

        builder.stream(
                sub_order_validations_topic.getName(),
                Consumed.with(sub_order_validations_topic.getKeySerde(), sub_order_validations_topic.getValueSerde())
            ).groupByKey()
            .aggregate(Order::newPending, (k, validate_sub_order, joined_order) -> {
                joined_order.addProduct(validate_sub_order.intoOrderProduct());
                joined_order.setOrderId(k);

                if (!validate_sub_order.isFullfilled()) {
                    joined_order.setStatus(OrderState.REJECTED);
                } else if (validate_sub_order.getSubOrder().getOrderParts() == joined_order.getSubOrderCount() && joined_order.isNot(OrderState.REJECTED)) {
                    joined_order.setStatus(OrderState.ALLOCATED);
                }

                return joined_order;
            }).toStream()
            .filter(
                (k, v) -> v.isNot(OrderState.PENDING)
            ).to(orders_topic.getName());
    }

    @Bean
    public void split_orders(StreamsBuilder builder) {
        var topics = Topics.build_topics();
        var orders_topic = topics.getOrders();
        var sub_order_validations_topic = topics.getSubOrderValidations();

        builder
            .stream(
                orders_topic.getName(),
                Consumed.with(orders_topic.getKeySerde(), orders_topic.getValueSerde())
            ).filter((id, order) -> order.is(OrderState.PENDING))
            .flatMap((id, order) -> order.intoSubOrders(id))
            .to(sub_order_validations_topic.getName());
    }

    @Bean void validate_sub_orders(StreamsBuilder builder) {
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
            .to(sub_order_validations_topic.getName());
    }
}
