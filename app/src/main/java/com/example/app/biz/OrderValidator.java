package com.example.app.biz;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import com.example.app.types.JoinedSubOrder;
import com.example.app.types.ValidatedSubOrder;
import com.example.app.util.Topics;

public class OrderValidator implements Processor<Long, JoinedSubOrder, Long, ValidatedSubOrder> {
    private ProcessorContext<Long, ValidatedSubOrder> context;
    private KeyValueStore<Long, Long> netInventory;

    @Override
    public void init(final ProcessorContext<Long, ValidatedSubOrder> context) {
        this.context = context;

        netInventory = context.getStateStore(
            Topics.build_topics()
                .getAllocatedInventory()
                .getName()
        );
    }

    @Override
    public void process(Record<Long, JoinedSubOrder> record) {
        var sub_order = record.value().getSubOrder();
        var warehouse_inventory = record.value().getCurrentInventory();
        var product = record.key();

        Long allocated = this.netInventory.get(product);

        if (allocated == null) {
            allocated = 0L;
        }

        var new_allocation = allocated + sub_order.getVolume();

        ValidatedSubOrder validated_order = null;

        var stock_is_available = warehouse_inventory - new_allocation >= 0;

        if (stock_is_available) {
            netInventory.put(product, new_allocation);
        }

        validated_order = ValidatedSubOrder.builder()
            .isFullfilled(stock_is_available)
            .subOrder(sub_order).build();

        context.forward(record.withKey(sub_order.getOrderId()).withValue(validated_order));
    }
}
