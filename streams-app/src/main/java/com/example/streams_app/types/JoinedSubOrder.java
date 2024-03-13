package com.example.streams_app.types;

import org.apache.flink.avro.generated.SubOrder;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class JoinedSubOrder {
    private SubOrder subOrder;
    private Long currentInventory;
}
