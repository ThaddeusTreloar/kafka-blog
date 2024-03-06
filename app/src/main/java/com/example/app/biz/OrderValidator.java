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
                .getNetInventory()
                .getName()
        );
    }

    @Override
    public void process(Record<Long, JoinedSubOrder> record) {
      //Look up locally 'reserved' stock from our state store
        var sub_order = record.value().getSubOrder();
        var total_inventory = record.value().getCurrentInventory();
        var product = record.key();

        Long allocated = this.netInventory.get(product);

        if (allocated == null) {
            allocated = 0L;
        }

        var new_allocation = allocated + sub_order.getVolume();

        ValidatedSubOrder validated_order = null;

        if (total_inventory - new_allocation >= 0) {
            netInventory.put(product, new_allocation);
            validated_order = ValidatedSubOrder.builder()
                .isFullfilled(true)
                .subOrder(sub_order).build();
        } else {
            validated_order = ValidatedSubOrder.builder()
                .isFullfilled(false)
                .subOrder(sub_order).build();
        }
        context.forward(record.withKey(sub_order.getId()).withValue(validated_order));
    }
}
