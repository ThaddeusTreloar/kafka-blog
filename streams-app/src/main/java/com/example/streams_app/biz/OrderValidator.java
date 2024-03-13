package com.example.streams_app.biz;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import com.example.streams_app.types.InventoryVolume;
import com.example.streams_app.types.JoinedSubOrder;
import com.example.streams_app.types.OrderId;
import com.example.streams_app.types.ProductId;
import com.example.streams_app.types.ValidatedSubOrder;
import com.example.streams_app.util.Topics;

public class OrderValidator implements Processor<ProductId, JoinedSubOrder, OrderId, ValidatedSubOrder> {
    private ProcessorContext<OrderId, ValidatedSubOrder> context;
    private KeyValueStore<ProductId, InventoryVolume> netInventory;

    @Override
    public void init(final ProcessorContext<OrderId, ValidatedSubOrder> context) {
        this.context = context;

        netInventory = context.getStateStore(Topics.ALLOCATED_INVENTORY_TOPIC);
    }

    @Override
    public void process(Record<ProductId, JoinedSubOrder> record) {
        var sub_order = record.value().getSubOrder();
        var warehouse_inventory = record.value().getCurrentInventory();
        var product = record.key();

        Long allocated = this.netInventory.get(product).getVolume();

        if (allocated == null) {
            allocated = 0L;
        }

        var new_allocation = allocated + sub_order.getVolume();
        
        var stock_is_available = warehouse_inventory - new_allocation >= 0;
        
        Long allocated_stock = sub_order.getVolume();

        if (stock_is_available) {
            netInventory.put(product, new InventoryVolume(new_allocation));
        } else {
            allocated_stock = 0L;
        }

        var order_id = new OrderId(sub_order.getOrderId());

        var validated_order = sub_order.withVolume(
            allocated_stock
        ).intoValidatedSubOrder(product.getProductId());

        context.forward(
            record
                .withKey(order_id)
                .withValue(validated_order)
        );
    }
}
