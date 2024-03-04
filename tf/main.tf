terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.64.0"
    }
  }
}

## Infrastructure

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key
  cloud_api_secret = var.confluent_cloud_api_secret
}

resource "confluent_environment" "example_environment" {
    display_name = "ExampleEnvironment"
}

resource "confluent_kafka_cluster" "example_cluster" {
    display_name = "basic_kafka_cluster"
    availability = "SINGLE_ZONE"
    cloud        = "AWS"
    region       = "ap-southeast-2"
    basic {}

    environment {
        id = confluent_environment.example_environment.id
    }
}

## Topics

resource "confluent_kafka_topic" "orders" {
  kafka_cluster {
    id = confluent_kafka_cluster.example_cluster.id
  }
  topic_name         = "orders"
  rest_endpoint      = confluent_kafka_cluster.example_cluster.rest_endpoint

  lifecycle {
    prevent_destroy = true
  }
}

resource "confluent_kafka_topic" "products" {
  kafka_cluster {
    id = confluent_kafka_cluster.example_cluster.id
  }
  topic_name         = "products"
  rest_endpoint      = confluent_kafka_cluster.example_cluster.rest_endpoint

  lifecycle {
    prevent_destroy = true
  }
}

## Service account

resource "confluent_service_account" "spring-app" {
  display_name = "spring-app-sa"
  description  = "Service Account for spring app"
}

resource "confluent_api_key" "spring-app-api-key" {
  display_name = "spring-app-api-key"
  description  = "Kafka API Key that is owned by 'spring-app' service account"

  owner {
    id          = confluent_service_account.spring-app.id
    api_version = confluent_service_account.spring-app.api_version
    kind        = confluent_service_account.spring-app.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.example_cluster.id
    api_version = confluent_kafka_cluster.example_cluster.api_version
    kind        = confluent_kafka_cluster.example_cluster.kind

    environment {
      id = confluent_environment.example_environment.id
    }
  }
}


## ACLs

resource "confluent_kafka_acl" "spring-app-acl" {
  kafka_cluster {
    id = confluent_kafka_cluster.example_cluster.id
  }
  resource_type = "TOPIC"
  resource_name = "spring-app-topic-acl"
  pattern_type  = "LITERAL"
  principal     = format("User:%s", confluent_service_account.spring-app.id)
  host          = "*"
  operation     = "ALL"
  permission    = "ALLOW"
}

