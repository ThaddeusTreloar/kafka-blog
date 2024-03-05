package com.example.app.util;

import org.apache.kafka.common.serialization.Serde;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor @Builder @Getter @ToString
public class Topic<K, V> {
    private String name;
    private Serde<K> keySerde;
    private Serde<V> valueSerde;
}
