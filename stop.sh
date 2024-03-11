#!/bin/sh

cd app

docker-compose -f rendered_docker_compose.yaml down

cd ../tf

terraform destroy

unset SPRING_KAFKA_API_KEY
unset SPRING_KAFKA_API_SECRET
unset SPRING_KAFKA_BOOTSTRAP_SERVERS
