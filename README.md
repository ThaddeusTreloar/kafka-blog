# Simplify Stream Processing

This is the demo tracked by the following [blog post](https://www.deloitte.com/au/en/services/consulting/blogs.html). You can run the demo yourself and follow along while reading.

## Dependencies

There are several tools you will need to run this demo.

### Docker

You will also need to ensure that you have a docker instance running on your machine.  
A guide for Docker setup can be found [here](https://docs.docker.com/desktop).

You will also need compose if you are running the start script with docker, instructions for install and setup can be found [here](https://docs.docker.com/compose/install/)

### Confluent Cli

For some of the configuration, you will need the confluent CLI, instructions can be found [here](https://docs.confluent.io/confluent-cli/current/install.html)

### Maven

All of the java binaries are built with maven, instructions for installation and setup can be found [here](https://maven.apache.org/install.html)

### Terraform

There infrastructure lifecycle is managed through terraform, instructions for installation and setup can be found [here](https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli)

## Getting a CC cloud key
  
In order to run this demo you will need a Confluent Cloud cloud api-key. This can be generated from the `confluent` cli app using the following commands.

```
$ confluent login
$ confluent api-key create --resource cloud
```

## Setting up terraform
  
```
export TF_VAR_confluent_cloud_api_key={{your cc key}}
export TF_VAR_confluent_cloud_api_secret={{your cc secret}}
```

By default the terraform script uses the `AWS` cloud provider in the `ap-southeast-2` region,  
and the `sgreg-4` (`ap-southeast-2` equivalent) region for schema registry.
If you want to run this from a different region you can set the following variables in your  
environment before running the `start.sh` script.

```
# Cloud provider
export TF_VAR_cloud_provider={{ AWS | AZURE | GCP }}

# Deployment region
export TF_VAR_deployment_region={{your deployment region}}

# Deployment region
export TF_VAR_schema_registry_region={{your schema registry deployment region}}
```

A list of cloud providers for each region can be found [here](https://docs.confluent.io/cloud/current/clusters/regions.html#cloud-providers-and-regions).

There isn't an online resource for schema registry regions, but you can list and find your region by use the following command:
```
confluent schema-registry region list
```

## Spinning up the deployment

Once you have completed the setup, run the following setup command:
```
start.sh
```
This will initialise the Confluent Cloud environment, along with all dependencies.  
It will then run the streaming apps from the inital deployment in the blog, ready to start tinkering.

A side note, if you are finding that corporate certificate authorities are causing issue with schema registry SSL handshakes, there is also a version of the start script that runs the containers locally
```
start-no-docker.sh
```

## Changing to the FlinkSQL queries

When you are migrating to the FlinkSQL queries, you will need to stop your kafka streams instance.
You can just ctl+c the terminal running your start script. Then, to restart the faker, just run:
```
start-faker.sh
```
or 
```
start-faker-no-docker.sh
```
If you aren't using docker.

Happy building!