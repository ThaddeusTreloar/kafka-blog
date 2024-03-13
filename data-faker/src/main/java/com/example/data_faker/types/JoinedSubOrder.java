package com.example.data_faker.types;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class JoinedSubOrder {
    private SubOrder subOrder;
    private Long currentInventory;
}
