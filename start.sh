#!/bin/sh

cd tf

terraform init
terraform apply

export SPRING_KAFKA_API_KEY=$(terraform output -json spring-app-credentials | jq -r .api_key)
export SPRING_KAFKA_API_SECRET=$(terraform output -json spring-app-credentials | jq -r .api_secret)
export SPRING_KAFKA_BOOTSTRAP_SERVERS=$(terraform output -json spring-app-credentials | jq -r .bootstrap_servers)

cd ..