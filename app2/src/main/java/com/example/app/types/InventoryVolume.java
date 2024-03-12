package com.example.app.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @AllArgsConstructor @Setter
public class InventoryVolume {
    private Long volume;

    public InventoryVolume addVolume(InventoryVolume other) {
        this.setVolume(this.getVolume() + other.getVolume());

        return this;
    }
}
