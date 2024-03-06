package com.example.app.types;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class Fulfillment {
    public FulfillmentStatus status;
    public Long product;

    public boolean is(FulfillmentStatus state) {
        return this.status.is(state);
    }
}
