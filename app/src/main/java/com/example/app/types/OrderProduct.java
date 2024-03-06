package com.example.app.types;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class OrderProduct {
    private Long id;
    private Long volume;

    public boolean hasNoVolume() {
        return this.getVolume() == 0;
    }
}
