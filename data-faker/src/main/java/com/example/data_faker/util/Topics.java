package com.example.data_faker.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.specific.SpecificRecord;
import org.apache.flink.avro.generated.Customer;
import org.apache.flink.avro.generated.CustomerId;
import org.apache.flink.avro.generated.Order;
import org.apache.flink.avro.generated.OrderId;

import com.example.data_faker.biz.KafkaEnv;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.Getter;

@Getter
public class Topics {
    private Topic<CustomerId, Customer> customers;
    private Topic<OrderId, Order> orders;

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

    public static String CUSTOMERS_TOPIC = "customers";
    public static String ORDERS_TOPIC = "orders";

    private <T extends SpecificRecord> SpecificAvroSerde<T> initSerde(Map<String, String> props, boolean isKey) {
        var serde = new SpecificAvroSerde<T>();
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

        this.customers = Topic.<CustomerId, Customer>builder()
            .name(CUSTOMERS_TOPIC)
            .keySerde(this.initSerde(props, true))
            .valueSerde(this.initSerde(props, false))
            .build();

        this.orders = Topic.<OrderId, Order>builder()
            .name(ORDERS_TOPIC)
            .keySerde(this.initSerde(props, true))
            .valueSerde(this.initSerde(props, false))
            .build();
    }
}