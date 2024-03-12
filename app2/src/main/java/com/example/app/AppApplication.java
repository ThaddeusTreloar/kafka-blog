package com.example.app;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.example.app.biz.KafkaEnv;
import com.example.app.types.Customer;
import com.example.app.types.CustomerId;
import com.example.app.types.Order;
import com.example.app.types.OrderId;
import com.example.app.types.OrderState;
import com.example.app.types.ProductVolume;
import com.example.app.util.Topics;
import com.github.javafaker.Faker;

import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;

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

		ArrayList<Customer> customers = new ArrayList<>();

		LongStream.range(0, 50).forEach(
			n -> {
				var customer_value = Customer.builder()
						.firstName(faker.name().firstName())
						.lastName(faker.name().lastName())
						.address(faker.address().streetAddress())
						.city(faker.address().city())
						.country(faker.address().country())
						.state(faker.address().state())
						.zip(faker.address().zipCode())
						.build();
				
				var customer_key = new CustomerId(n);

				System.out.println("here");

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

		while (true) {
			var l = Long.valueOf(faker.number().numberBetween(0, 50));

			List<ProductVolume> products = new ArrayList<ProductVolume>();

			IntStream.range(0, faker.number().numberBetween(0, 5))
				.forEach(
					n -> {
						var op = ProductVolume.builder()
							.productId(Long.valueOf(faker.number().numberBetween(0, 100)))
							.volume(Long.valueOf(faker.number().numberBetween(0, 5)))
							.build();

						products.add(op);
					}
				);

			var order_val = Order.builder()
				.customerId(l)
				.products(products)
				.status(OrderState.ALLOCATED)
				.build();

			var record = new ProducerRecord<>("orders", new OrderId(order_num), order_val);

			var f = order_producer.send(record);

			try {
				f.get();
				order_num++;
			} catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}
		}
	}

}
