package com.example.data_faker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.apache.flink.avro.generated.Customer;
import org.apache.flink.avro.generated.CustomerId;
import org.apache.flink.avro.generated.Order;
import org.apache.flink.avro.generated.OrderId;
import org.apache.flink.avro.generated.ProductVolume;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.example.data_faker.biz.KafkaEnv;
import com.example.data_faker.types.OrderState;
import com.example.data_faker.util.Topics;
import com.github.javafaker.Faker;

import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppApplication {

	public static void main(String[] args) {
		var config = new KafkaEnv();

		
		var topics = new Topics(config);
		
		var props = config.intoConfigMap();
		
		
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class);
		
		var customer_producer = new KafkaProducer<CustomerId, Customer>(
			config.intoConfigMap(), 
			topics.getCustomers().getKeySerde().serializer(),
			topics.getCustomers().getValueSerde().serializer()
		);
			
		Faker faker = new Faker();

		LongStream.range(0, 50).forEach(
			n -> {
				var customer_value = Customer.newBuilder()
						.setFirstName(faker.name().firstName())
						.setLastName(faker.name().lastName())
						.setAddress(faker.address().streetAddress())
						.setCity(faker.address().city())
						.setCountry(faker.address().country())
						.setState(faker.address().state())
						.setZip(faker.address().zipCode())
						.build();
				
				var customer_key = new CustomerId(n);

				log.info(customer_key.toString());
				log.info(customer_value.toString());

				var record = new ProducerRecord<>("customers", customer_key, customer_value);

				var f = customer_producer.send(record);

				try {
					f.get();
				} catch (Exception e) {
					System.out.println(e);
					System.exit(0);
				}
			}
		);

		customer_producer.close();

		var order_producer = new KafkaProducer<OrderId, Order>(
			config.intoConfigMap(), 
			topics.getOrders().getKeySerde().serializer(),
			topics.getOrders().getValueSerde().serializer()
		);

		Long order_num = 0L;

		try {
			while (true) {
				var l = Long.valueOf(faker.number().numberBetween(0, 50));
	
				List<ProductVolume> products = new ArrayList<ProductVolume>();
	
				IntStream.range(0, faker.number().numberBetween(0, 5))
					.forEach(
						n -> {
							var op = ProductVolume.newBuilder()
								.setProductId(Long.valueOf(faker.number().numberBetween(0, 100)))
								.setVolume(Long.valueOf(faker.number().numberBetween(0, 5)))
								.build();
	
							products.add(op);
						}
					);
	
				var order_val = Order.newBuilder()
					.setCustomerId(l)
					.setProducts(products)
					.setStatus(OrderState.ALLOCATED)
					.build();

				var order_key = new OrderId(order_num);

				log.info(order_key.toString());
				log.info(order_val.toString());
	
				var record = new ProducerRecord<>("orders", order_key, order_val);
	
				var f = order_producer.send(record);
	
				f.get();
				order_num++;
			}
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		} finally {
			order_producer.close();
		}
	}
}
