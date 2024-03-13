
output "spring-app-credentials" {
    value = {
        api_key = confluent_api_key.spring-app-api-key.id
        api_secret = confluent_api_key.spring-app-api-key.secret
        bootstrap_servers = confluent_kafka_cluster.example_cluster.bootstrap_endpoint
        schema_registry_url = confluent_schema_registry_cluster.schema-registry.rest_endpoint
        schema_user = confluent_api_key.spring-app-schema-registry-api-key.id
        schema_pass = confluent_api_key.spring-app-schema-registry-api-key.secret
    }

    sensitive = true
}