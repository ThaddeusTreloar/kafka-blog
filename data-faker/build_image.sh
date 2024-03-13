#!/bin/sh

mvn generate-sources
mvn install -DskipTests
docker build -t data_faker:0.0.1 -f app.Dockerfile .
