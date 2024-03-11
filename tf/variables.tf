variable "confluent_cloud_api_key" {
    description = "Confluent Cloud API Key"
    type = string
}

variable "confluent_cloud_api_secret" {
    description = "Confluent Cloud API secret"
    type = string
}

variable "cloud_provider" {
    description = "Confluent Cloud API secret"
    type = string
    default = "AWS"
}

variable "deployment_region" {
    description = "Confluent Cloud API secret"
    type = string
    default = "ap-southeast-2"
}