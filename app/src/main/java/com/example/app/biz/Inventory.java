package com.example.app.biz;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.example.app.types.Order;
import com.example.app.types.OrderState;
import com.example.app.util.Topics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.java.Log;

@Log
@Configuration
public class Inventory {

    public static String RESERVED_STOCK_STORE_NAME = "reserved-stock";
    
    @Bean
    public FactoryBean<StreamsBuilder> init_builder(KafkaStreamsConfiguration config) {
        var factory_bean = new StreamsBuilderFactoryBean(config);

        factory_bean.setAutoStartup(true);

        return factory_bean;
    }

    @Bean
    public KStream<String, ?> inventory_stream(StreamsBuilder builder) {
        var topics = Topics.build_topics();

        var confirmed_orders_topic = topics.getOrderValidations();
        var inventory_topic = topics.getInventory();
        var orders_topics = topics.getOrders();

        var orders = builder
            .stream(
                orders_topics.getName(),
                Consumed.with(orders_topics.getKeySerde(), orders_topics.getValueSerde())
            );

        var inventory = builder
            .globalTable(
                inventory_topic.getName(),
                Consumed.with(inventory_topic.getKeySerde(), inventory_topic.getValueSerde())
            );

        //Create a store to reserve inventory whilst the order is processed.
        //This will be prepopulated from Kafka before the service starts processing
        final StoreBuilder<KeyValueStore<Double, Long>> reservedStock = Stores
            .keyValueStoreBuilder(
                Stores.persistentKeyValueStore(RESERVED_STOCK_STORE_NAME), 
                Serdes.Double(), 
                Serdes.Long()
            )
            .withLoggingEnabled(new HashMap<>());
        
        builder.addStateStore(reservedStock);

        orders.filter((id, order) -> order.is(OrderState.PENDING))
            .join(warehouseInventory, KeyValue::new, Joined.with(Topics.WAREHOUSE_INVENTORY.keySerde(),
                Topics.ORDERS.valueSerde(), Serdes.Integer()))
            .process(InventoryValidator::new, RESERVED_STOCK_STORE_NAME)
            .to(
                Topics.ORDER_VALIDATIONS.name(),
                Produced.with(
                    Topics.ORDER_VALIDATIONS.keySerde(), Topics.ORDER_VALIDATIONS.valueSerde()
            ));
    }
}
