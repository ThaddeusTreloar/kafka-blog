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

cd ../spring-app

export DOCKER_DEFAULT_PLATFORM=linux/amd64

./build_image.sh

cd ../data-faker

./build_image.sh

cd ..

cat ./docker-compose.yaml \
    | sed "s|SPRING_KAFKA_API_KEY__|$SPRING_KAFKA_API_KEY|g" \
    | sed "s|SPRING_KAFKA_API_SECRET__|$SPRING_KAFKA_API_SECRET|g" \
    | sed "s|SPRING_KAFKA_BOOTSTRAP_SERVERS__|$SPRING_KAFKA_BOOTSTRAP_SERVERS|g" \
    | sed "s|SPRING_KAFKA_SCHEMA_REGISTRY_URL__|$SPRING_KAFKA_SCHEMA_REGISTRY_URL|g" \
    | sed "s|SPRING_KAFKA_SCHEMA_USER__|$SPRING_KAFKA_SCHEMA_USER|g" \
    | sed "s|SPRING_KAFKA_SCHEMA_PASS__|$SPRING_KAFKA_SCHEMA_PASS|g" > rendered_docker_compose.yaml

docker-compose -f rendered_docker_compose.yaml up