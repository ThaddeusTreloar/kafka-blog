#!/bin/sh

cd tf

terraform init
terraform apply

export SPRING_KAFKA_API_KEY=$(terraform output -json spring-app-credentials | jq -r .api_key)
export SPRING_KAFKA_API_SECRET=$(terraform output -json spring-app-credentials | jq -r .api_secret)
export SPRING_KAFKA_BOOTSTRAP_SERVERS=$(terraform output -json spring-app-credentials | jq -r .bootstrap_servers)
export SPRING_KAFKA_SCHEMA_REGISTRY_URL=$(terraform output -json spring-app-credentials | jq -r .schema_registry_url)
export SPRING_KAFKA_SCHEMA_USER=$(terraform output -json spring-app-credentials | jq -r .schema_user)
export SPRING_KAFKA_SCHEMA_PASS=$(terraform output -json spring-app-credentials | jq -r .schema_pass)

cd ../streams-app
mvn install

cd ../data-faker
mvn install

cd ..
(trap 'kill 0' SIGINT; java -jar streams-app/target/streams-app-0.0.1.jar & java -jar data-faker/target/data-faker-0.0.1.jar)