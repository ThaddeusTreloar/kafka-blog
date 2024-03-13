package com.example.streams_app.util;

import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.units.qual.t;

import com.example.streams_app.biz.KafkaEnv;
import com.example.streams_app.types.Customer;
import com.example.streams_app.types.CustomerId;
import com.example.streams_app.types.InventoryVolume;
import com.example.streams_app.types.LogisticsOrder;
import com.example.streams_app.types.Order;
import com.example.streams_app.types.OrderId;
import com.example.streams_app.types.ProductId;
import com.example.streams_app.types.SubOrder;
import com.example.streams_app.types.ValidatedSubOrder;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde;
import lombok.Getter;

@Getter
public class Topics {
    private Topic<ProductId, InventoryVolume> warehouseInventory;
    private Topic<ProductId, InventoryVolume> allocatedInventory;
    private Topic<OrderId, LogisticsOrder> logisticsOrder;
    private Topic<CustomerId, Customer> customers;
    private Topic<OrderId, Order> orders;
    private Topic<ProductId, SubOrder> subOrders;
    private Topic<OrderId, ValidatedSubOrder> subOrderValidations;

    /*
     * inventory.key = long
     * inventory.value = long
     * allocated_inventory.key = long
     * allocated_inventory.value = long
     * orders.key = long
     * orders.value = {
     *  orderId: long,
     *  status: string (PENDING, ALLOCATED, REJECTED),
     *  products: list{
     *      id: long,
     *      volume: long
     *  }
     * }
     * subOrders.key = long
     * subOrders.value = {
     *  orderId: long,
     *  orderParts: integer,
     *  volume: long,
     * }
     * subOrderValidations.key = long
     * subOrderValidations.value = {
     *  orderId: long,
     *  orderParts: integer,
     *  volume: long,   
     * }
     */

    public static String WAREHOUSE_INVENTORY_TOPIC = "warehouse_inventory";
    public static String ALLOCATED_INVENTORY_TOPIC = "allocated_inventory";
    public static String LOGISTICS_ORDER_TOPIC = "logistics_orders";
    public static String CUSTOMERS_TOPIC = "customers";
    public static String ORDERS_TOPIC = "orders";
    public static String SUB_ORDERS_TOPIC = "sub_orders";
    public static String SUB_ORDER_VALIDATIONS_TOPIC = "sub_order_validations";

    private <T> KafkaJsonSchemaSerde<T> initSerde(Map<String, String> props, String class_name, boolean isKey) {
        if (isKey) {
            props.put("KafkaJsonSchemaDeserializerConfig.JSON_KEY_TYPE", class_name);
            props.put("json.key.type", class_name);
        } else {
            props.put("KafkaJsonSchemaDeserializerConfig.JSON_VALUE_TYPE", class_name);
            props.put("json.value.type", class_name);
        }

        var serde = new KafkaJsonSchemaSerde<T>();
        serde.configure(props, isKey);

        return serde;
    }

    private <T> KafkaJsonSchemaSerde<T> initSerde(Map<String, String> props, boolean isKey) {
        if (isKey) {
            props.put("KafkaJsonSchemaDeserializerConfig.JSON_KEY_TYPE", t.class.getName());
        } else {
            props.put("KafkaJsonSchemaDeserializerConfig.JSON_VALUE_TYPE", t.class.getName());
        }

        var serde = new KafkaJsonSchemaSerde<T>();
        serde.configure(props, isKey);

        return serde;
    }

    public Topics(KafkaEnv kafka_config) {
        var props = new HashMap<String, String>();

        props.put("schema.registry.url", kafka_config.getSchemaRegistryUrl());
        props.put(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
        props.put(
            SchemaRegistryClientConfig.USER_INFO_CONFIG, 
            new StringBuilder()
                .append(kafka_config.getSchemaRegistryUser())
                .append(":")
                .append(kafka_config.getSchemaRegistryPass())
                .toString()
        );

        this.warehouseInventory = Topic.<ProductId, InventoryVolume>builder()
            .name(WAREHOUSE_INVENTORY_TOPIC)
            .keySerde(this.initSerde(props, ProductId.class.getCanonicalName(), true))
            .valueSerde(this.initSerde(props, InventoryVolume.class.getCanonicalName(), false))
            .build();

        this.allocatedInventory = Topic.<ProductId, InventoryVolume>builder()
            .name(ALLOCATED_INVENTORY_TOPIC)
            .keySerde(this.initSerde(props, ProductId.class.getCanonicalName(), true))
            .valueSerde(this.initSerde(props, InventoryVolume.class.getCanonicalName(), false))
            .build();

        this.customers = Topic.<CustomerId, Customer>builder()
            .name(CUSTOMERS_TOPIC)
            .keySerde(this.initSerde(props, CustomerId.class.getCanonicalName(), true))
            .valueSerde(this.initSerde(props, Customer.class.getCanonicalName(), false))
            .build();

        this.logisticsOrder = Topic.<OrderId, LogisticsOrder>builder()
            .name(LOGISTICS_ORDER_TOPIC)
            .keySerde(this.initSerde(props, OrderId.class.getCanonicalName(), true))
            .valueSerde(this.initSerde(props, LogisticsOrder.class.getCanonicalName(), false))
            .build();

        this.orders = Topic.<OrderId, Order>builder()
            .name(ORDERS_TOPIC)
            .keySerde(this.initSerde(props, OrderId.class.getCanonicalName(), true))
            .valueSerde(this.initSerde(props, Order.class.getCanonicalName(), false))
            .build();

        this.subOrders = Topic.<ProductId, SubOrder>builder()
            .name(SUB_ORDERS_TOPIC)
            .keySerde(this.initSerde(props, ProductId.class.getCanonicalName(), true))
            .valueSerde(this.initSerde(props, SubOrder.class.getCanonicalName(), false))
            .build();

        this.subOrderValidations = Topic.<OrderId, ValidatedSubOrder>builder()
            .name(SUB_ORDER_VALIDATIONS_TOPIC)
            .keySerde(this.initSerde(props, OrderId.class.getCanonicalName(), true))
            .valueSerde(this.initSerde(props, ValidatedSubOrder.class.getCanonicalName(), false))
            .build();
    }
}