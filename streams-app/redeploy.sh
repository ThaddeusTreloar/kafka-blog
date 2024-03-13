#!/bin/sh

./build_image.sh

docker-compose -f rendered_docker_compose.yaml down
docker-compose -f rendered_docker_compose.yaml up