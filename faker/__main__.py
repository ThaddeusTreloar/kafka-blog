import argparse
import random
import json
import time
from kafka import KafkaProducer
from os import environ
from schema_registry.client import SchemaRegistryClient, schema
from schema_registry.serializers import JsonMessageSerializer
from httpx import Auth

# Define schemas
schemas = {
    "inventory": {
        "key": {
            "productId": "long"
        },
        "value": ("long", 10)
    },
    "orders": {
        "key": {
            "orderId": "long"
        },
        "value": {
            "status": "string",
            "products": [{
                "id": "long",
                "volume": ("long", 10)
            }]
        }
    },
}

schema_reg = {
    "order_key": schema.JsonSchema({
        "fields": [
            {
            "name": "orderId",
            "type": "long"
            }
        ],
        "name": "record",
        "namespace": "org.apache.flink.avro.generated",
        "type": "object"
    }),
    "order_value": schema.JsonSchema({
        "properties": {
            "products": {
            "connect.index": 1,
            "oneOf": [
                {
                "type": "null"
                },
                {
                "items": [
                    {
                    "oneOf": [
                        {
                        "type": "null"
                        },
                        {
                        "properties": {
                            "productId": {
                            "connect.index": 0,
                            "oneOf": [
                                {
                                "type": "null"
                                },
                                {
                                "connect.type": "int64",
                                "type": "number"
                                }
                            ]
                            },
                            "volume": {
                            "connect.index": 1,
                            "oneOf": [
                                {
                                "type": "null"
                                },
                                {
                                "connect.type": "int64",
                                "type": "number"
                                }
                            ]
                            }
                        },
                        "title": "Record_products",
                        "type": "object"
                        }
                    ]
                    }
                ],
                "type": "array"
                }
            ]
            },
            "status": {
            "connect.index": 0,
            "type": "string"
            }
        },
        "required": [
            "status"
        ],
        "title": "Record",
        "type": "object"
    }),
    "inventory_key": schema.JsonSchema({
        "fields": [
            {
            "name": "productId",
            "type": "long"
            }
        ],
        "name": "record",
        "namespace": "org.apache.flink.avro.generated",
        "type": "object"
    }),
    "inventory_value": schema.JsonSchema({
        "properties": {
            "volume": {
            "connect.index": 0,
            "connect.type": "int64",
            "type": "number"
            }
        },
        "required": [
            "volume"
        ],
        "title": "Record",
        "type": "object"
    })
}

# Generate random data based on schema
def generate_random_data(schema, list_len=10, l_upper_bound=1000, i_upper_bound=1000):
    if isinstance(schema, dict):
        data = {}
        for key, value in schema.items():
            data[key] = generate_random_data(value)
        return data
    elif isinstance(schema, list):
        l = random.randint(1, list_len)
        return [generate_random_data(schema[0]) for x in range(l)]
    elif isinstance(schema, tuple):
        if schema[0] == "long":
            l_upper_bound = schema[1]
            return random.randint(1, l_upper_bound)
        elif schema[0] == "integer":
            i_upper_bound = schema[1]
            return random.randint(1, i_upper_bound)
    elif schema == "long":
        return random.randint(1, l_upper_bound)
    elif schema == "integer":
        return random.randint(1, i_upper_bound)
    elif schema == "string":
        return "PENDING" #random.choice(["PENDING", "ALLOCATED", "REJECTED"])

# Push data to Kafka cluster
def push_to_kafka(topic, key_ser, value_ser, key_schema, value_schema,data):
    api_key = environ.get("SPRING_KAFKA_API_KEY")
    api_secret = environ.get("SPRING_KAFKA_API_SECRET")
    bootstrap_servers = environ.get("SPRING_KAFKA_BOOTSTRAP_SERVERS")
    schema_reg_url = environ.get("SPRING_KAFKA_SCHEMA_REGISTRY_URL")

    producer = KafkaProducer(bootstrap_servers=bootstrap_servers,
                             key_serializer=lambda x: key_ser.encode_record_with_schema,
                             value_serializer=lambda x: json.dumps(x).encode('utf-8'))
    producer.send(topic, key=data[key_schema], value=data[value_schema])
    producer.flush()

def main():
    # Command line argument parsing
    parser = argparse.ArgumentParser(description="Push randomised test data into a Kafka cluster.")
    parser.add_argument("topic", help="Topic name to which data will be pushed.")
    parser.add_argument("--stdout", action="store_true", help="Send output to stdout instead of Kafka.")
    parser.add_argument("--count", type=int, default=1, help="Number of records to generate (default: 1).")
    parser.add_argument("--interval", type=float, default=0, help="Random interval between records (default: 0).")
    args = parser.parse_args()

    # Get schema for the specified topic
    schema = schemas.get(args.topic)
    if schema is None:
        print(f"Error: Schema not found for topic '{args.topic}'")
        return

    if args.count < 0:
        args.count = 2**32

    api_key = environ.get("SPRING_KAFKA_API_KEY")
    api_secret = environ.get("SPRING_KAFKA_API_SECRET")
    bootstrap_servers = environ.get("SPRING_KAFKA_BOOTSTRAP_SERVERS")
    schema_reg_url = environ.get("SPRING_KAFKA_SCHEMA_REGISTRY_URL")

    client = SchemaRegistryClient (url=schema_reg_url, auth=Auth(username=api_key, password=api_secret))

    


    key_schema = client.get_schema(f"{args.topic}-key", version='latest')
    
    if key_schema is None:
        client.register(schema_reg[f"{args.topic}-key"])
        key_schema = schema_reg[f"{args.topic}-key"]

    value_schema = client.get_schema(f"{args.topic}-value", version='latest')

    if value_schema is None:
        client.register(schema_reg[f"{args.topic}-value"])
        value_schema = schema_reg[f"{args.topic}-value"]

    key_ser = JsonMessageSerializer(client)
    value_ser = JsonMessageSerializer(client)

    # Generate and push or print the specified number of records with random interval
    for i in range(args.count):
        data = generate_random_data(schema["value"])
        key_data = generate_random_data(schema["key"])
        output = {f"{args.topic}.key": key_data, f"{args.topic}.value": data}

        if args.topic == "orders":
            data["orderId"] = key_data

        if args.stdout:
            ser_val = json.dumps(data)
            print(f"{key_data}:{ser_val}")
        else:
            print(f"Pushing data to topic '{args.topic}': Key: {key_data}, Value: {data}")
            push_to_kafka(args.topic, key_ser, value_ser, key_schema, value_schema, output)

        if args.interval and i < args.count - 1:
            interval = random.uniform(0, args.interval)
            time.sleep(interval/1000)

if __name__ == "__main__":
    main()