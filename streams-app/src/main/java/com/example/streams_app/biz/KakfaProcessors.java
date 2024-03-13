package com.example.streams_app.biz;

import java.util.HashMap;
import java.util.List;

import org.apache.flink.avro.generated.CustomerId;
import org.apache.flink.avro.generated.InventoryVolume;
import org.apache.flink.avro.generated.LogisticsOrder;
import org.apache.flink.avro.generated.Order;
import org.apache.flink.avro.generated.ProductId;
import org.apache.flink.avro.generated.ProductVolume;
import org.apache.flink.avro.generated.SubOrder;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.streams_app.types.JoinedSubOrder;
import com.example.streams_app.types.OrderState;
import com.example.streams_app.types.PartialOrder;
import com.example.streams_app.util.Topics;

@Component
public class KakfaProcessors {

    public static List<KeyValue<ProductId, InventoryVolume>> productVolumeIntoInventoryVolume(List<ProductVolume> list) {
        return list.stream()
            .map((v) -> new KeyValue<>(
                new ProductId(v.getProductId()),
                new InventoryVolume(v.getVolume())
            ))
            .toList();
    }

    public static List<KeyValue<ProductId, SubOrder>> productsIntoSubOrders(List<ProductVolume> products, Long customer_id, Long order_id) {
        return products.stream()
            .map(
                (product) -> {
                    var product_id = new ProductId(product.getProductId());
                    var new_suborder = SubOrder.newBuilder()
                        .setCustomerId(customer_id)
                        .setOrderId(order_id)
                        .setVolume(product.getVolume())
                        .setOrderParts(products.size())
                        .build();

                    return new KeyValue<ProductId, SubOrder>(product_id, new_suborder);
                }
            ).toList();
    }

    public static boolean containsUnallocatedOrder(List<ProductVolume> products) {
        return products.stream()
            .anyMatch((p) -> p.getVolume().equals(0L));
    }

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
            (k, v) -> v.getStatus().toString().equals(OrderState.ALLOCATED)
        ).join(
            customers,
            (k, v) -> new CustomerId(v.getCustomerId()),
            (order, customer) -> LogisticsOrder.newBuilder()
                .setFirstName(customer.getFirstName())
                .setLastName(customer.getLastName())
                .setAddress(customer.getAddress())
                .setCity(customer.getCity())
                .setCountry(customer.getCountry())
                .setState(customer.getState())
                .setZip(customer.getZip())
                .setStatus(order.getStatus())
                .setProducts(order.getProducts())
                .build()
        ).to(
            logistics_order_topic.getName(),
            Produced.with(
                logistics_order_topic.getKeySerde(), 
                logistics_order_topic.getValueSerde()
            )
        );
    }

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
                joined_order.addProduct(
                    ProductVolume.newBuilder()
                        .setProductId(validate_sub_order.getProductId())
                        .setVolume(validate_sub_order.getVolume())
                        .build());
                joined_order.setOrderParts(validate_sub_order.getOrderParts());
                joined_order.setCustomerId(validate_sub_order.getCustomerId());
                
                return joined_order;
            }).toStream();

        var agg_orders = validated_orders.filter((k, v) -> v.hasAllProducts());

        agg_orders.filter((k, v) -> !containsUnallocatedOrder(v.getProducts()))
            .mapValues((k, v) -> Order.newBuilder()
                .setCustomerId(v.getCustomerId())
                .setProducts(v.getProducts())
                .setStatus("ALLOCATED")
                .build()
            )
            .to(
                orders_topic.getName(),
                Produced.with(
                    orders_topic.getKeySerde(), 
                    orders_topic.getValueSerde()
                )
            );

        agg_orders.filter((k, v) -> containsUnallocatedOrder(v.getProducts()))
            .mapValues((k, v) -> Order.newBuilder()
                .setCustomerId(v.getCustomerId())
                .setProducts(v.getProducts())
                .setStatus("REJECTED")
                .build()
            ).to(
                orders_topic.getName(),
                Produced.with(
                    orders_topic.getKeySerde(), 
                    orders_topic.getValueSerde()
                )
            );

        agg_orders.filter((k, v) -> containsUnallocatedOrder(v.getProducts()))
            .flatMap(
                (k, v) -> productVolumeIntoInventoryVolume(v.getProducts())
            ).filter(
                (k, v) -> v.getVolume() == 0L
            ).to(
                inventory_topic.getName(),
                Produced.with(
                    inventory_topic.getKeySerde(), 
                    inventory_topic.getValueSerde()
                )
            );
    }

    @Autowired
    public void split_orders(StreamsBuilder builder, Topics kafkaTopicsConfig) {
        var orders_topic = kafkaTopicsConfig.getOrders();
        var sub_orders_topic = kafkaTopicsConfig.getSubOrders();

        // Split order into product sub_orders
        builder.stream(
                orders_topic.getName(),
                Consumed.with(orders_topic.getKeySerde(), orders_topic.getValueSerde())
            ).filter((id, order) -> order.getStatus().toString().equals(OrderState.PENDING))
            .flatMap((id, order) -> productsIntoSubOrders(
                order.getProducts(), order.getCustomerId(), id.getOrderId()
            ))
            .to(
                sub_orders_topic.getName(),
                Produced.with(
                    sub_orders_topic.getKeySerde(), 
                    sub_orders_topic.getValueSerde()
                )
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
            .reduce((left, right) -> new InventoryVolume(left.getVolume() + right.getVolume()));

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
                sub_order_validations_topic.getName(),
                Produced.with(
                    sub_order_validations_topic.getKeySerde(),
                    sub_order_validations_topic.getValueSerde()
                )
            );
    }
}