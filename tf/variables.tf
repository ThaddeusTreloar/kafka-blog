variable "confluent_cloud_api_key" {
    description = "Confluent Cloud API Key"
    type = string
}

variable "confluent_cloud_api_secret" {
    description = "Confluent Cloud API secret"
    type = string
}

variable "cloud_provider" {
    description = "Cloud provider to deploy resources on"
    type = string
    default = "AWS"
}

variable "deployment_region" {
    description = "Deployment region for your resources. This will change based on your cloud provider"
    type = string
    default = "ap-southeast-2"
}

variable "schema_registry_region" {
    description = "Deployment region for your schema registry cluster."
    type = string
    default = "sgreg-4"
}