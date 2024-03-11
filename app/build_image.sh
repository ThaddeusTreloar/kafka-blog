#!/bin/sh

mvn install -DskipTests
docker build -t streams_app:0.0.1 -f app.Dockerfile .
