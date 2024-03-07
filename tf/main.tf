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

## Service account

resource "confluent_service_account" "cluster-manager" {
  display_name = "cluster-manager-sa"
  description  = "Service Account for managing cluster settings"
}

resource "confluent_role_binding" "cluster-manager-rbac" {
    principal   = "User:${confluent_service_account.cluster-manager.id}"
    role_name   = "CloudClusterAdmin"
    crn_pattern = confluent_kafka_cluster.example_cluster.rbac_crn
}

resource "confluent_api_key" "cluster-manager-api-key" {
  display_name = "cluster-manager-api-key"
  description  = "Kafka API Key that is owned by 'cluster-manager' service account"

  owner {
    id          = confluent_service_account.cluster-manager.id
    api_version = confluent_service_account.cluster-manager.api_version
    kind        = confluent_service_account.cluster-manager.kind
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

  rest_endpoint      = confluent_kafka_cluster.example_cluster.rest_endpoint

  credentials {
    key = confluent_api_key.cluster-manager-api-key.id
    secret = confluent_api_key.cluster-manager-api-key.secret
  }
}

## Topics

resource "confluent_kafka_topic" "inventory-topic" {
  kafka_cluster {
    id = confluent_kafka_cluster.example_cluster.id
  }
  topic_name         = "inventory"
  rest_endpoint      = confluent_kafka_cluster.example_cluster.rest_endpoint

  lifecycle {
    prevent_destroy = true
  }

  credentials {
    key = confluent_api_key.cluster-manager-api-key.id
    secret = confluent_api_key.cluster-manager-api-key.secret
  }
}

resource "confluent_kafka_topic" "allocated_inventory-topic" {
  kafka_cluster {
    id = confluent_kafka_cluster.example_cluster.id
  }
  topic_name         = "allocated_inventory"
  rest_endpoint      = confluent_kafka_cluster.example_cluster.rest_endpoint

  lifecycle {
    prevent_destroy = true
  }

  credentials {
    key = confluent_api_key.cluster-manager-api-key.id
    secret = confluent_api_key.cluster-manager-api-key.secret
  }
}

resource "confluent_kafka_topic" "orders-topic" {
  kafka_cluster {
    id = confluent_kafka_cluster.example_cluster.id
  }
  topic_name         = "orders"
  rest_endpoint      = confluent_kafka_cluster.example_cluster.rest_endpoint

  lifecycle {
    prevent_destroy = true
  }

  credentials {
    key = confluent_api_key.cluster-manager-api-key.id
    secret = confluent_api_key.cluster-manager-api-key.secret
  }
}

resource "confluent_kafka_topic" "sub_orders-topic" {
  kafka_cluster {
    id = confluent_kafka_cluster.example_cluster.id
  }
  topic_name         = "sub_orders"
  rest_endpoint      = confluent_kafka_cluster.example_cluster.rest_endpoint

  lifecycle {
    prevent_destroy = true
  }

  credentials {
    key = confluent_api_key.cluster-manager-api-key.id
    secret = confluent_api_key.cluster-manager-api-key.secret
  }
}

resource "confluent_kafka_topic" "sub_order_validations-topic" {
  kafka_cluster {
    id = confluent_kafka_cluster.example_cluster.id
  }
  topic_name         = "sub_order_validations"
  rest_endpoint      = confluent_kafka_cluster.example_cluster.rest_endpoint

  lifecycle {
    prevent_destroy = true
  }

  credentials {
    key = confluent_api_key.cluster-manager-api-key.id
    secret = confluent_api_key.cluster-manager-api-key.secret
  }
}