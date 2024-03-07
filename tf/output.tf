
output "spring-app-credentials" {
    value = {
        api_key = confluent_api_key.spring-app-api-key.id
        api_secret = confluent_api_key.spring-app-api-key.secret
        bootstrap_servers = confluent_kafka_cluster.example_cluster.bootstrap_endpoint
    }

    sensitive = true
}