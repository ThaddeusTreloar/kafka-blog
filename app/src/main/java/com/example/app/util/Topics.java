package com.example.app.util;

import java.util.HashMap;
import java.util.Map;

import com.example.app.biz.KafkaEnv;
import com.example.app.types.InventoryVolume;
import com.example.app.types.Order;
import com.example.app.types.OrderId;
import com.example.app.types.ProductId;
import com.example.app.types.SubOrder;
import com.example.app.types.ValidatedSubOrder;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde;
import lombok.Getter;

@Getter
public class Topics {
    private Topic<ProductId, InventoryVolume> warehouseInventory;
    private Topic<ProductId, InventoryVolume> allocatedInventory;
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

    public static String WAREHOUSE_INVENTORY_TOPIC = "inventory";
    public static String ALLOCATED_INVENTORY_TOPIC = "allocated_inventory";
    public static String ORDERS_TOPIC = "orders";
    public static String SUB_ORDERS_TOPIC = "sub_orders";
    public static String SUB_ORDER_VALIDATIONS_TOPIC = "sub_order_validations";

    private <T> KafkaJsonSchemaSerde<T> initSerde(Map<String, String> props, boolean isKey) {
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
                .append(kafka_config.getApiKey())
                .append(":")
                .append(kafka_config.getApiSecret())
                .toString()
        );

        this.warehouseInventory = Topic.<ProductId, InventoryVolume>builder()
            .name(WAREHOUSE_INVENTORY_TOPIC)
            .keySerde(this.initSerde(props, true))
            .valueSerde(this.initSerde(props, false))
            .build();

        this.allocatedInventory = Topic.<ProductId, InventoryVolume>builder()
            .name(ALLOCATED_INVENTORY_TOPIC)
            .keySerde(this.initSerde(props, true))
            .valueSerde(this.initSerde(props, false))
            .build();

        this.orders = Topic.<OrderId, Order>builder()
            .name(ORDERS_TOPIC)
            .keySerde(this.initSerde(props, true))
            .valueSerde(this.initSerde(props, false))
            .build();

        this.subOrders = Topic.<ProductId, SubOrder>builder()
            .name(SUB_ORDERS_TOPIC)
            .keySerde(this.initSerde(props, true))
            .valueSerde(this.initSerde(props, false))
            .build();

        this.subOrderValidations = Topic.<OrderId, ValidatedSubOrder>builder()
            .name(SUB_ORDER_VALIDATIONS_TOPIC)
            .keySerde(this.initSerde(props, true))
            .valueSerde(this.initSerde(props, false))
            .build();
    }
}