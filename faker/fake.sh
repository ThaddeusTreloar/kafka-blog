#!/bin/sh

docker run \   
  --env-file license.env \
  -v $(pwd)/customers.json:/home/config.json \
  shadowtraffic/shadowtraffic:latest \
  --config /home/config.json --sample 50 -d

docker run \
  --env-file license.env \
  -v $(pwd)/orders.json:/home/config.json \
  shadowtraffic/shadowtraffic:latest \
  --config /home/config.json \
  --watch --sample 10