# Simplify Stream Processing

This is the demo tracked by the following [blog post](https://www.deloitte.com/au/en/services/consulting/blogs.html). You can run the demo yourself and follow  
along while reading.

## Getting a CC cloud key
  
In order to run this demo you will need a Confluent Cloud cloud api-key. This can be generated  
from the `confluent` cli app using the following commands.

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
export TF_VAR_deployment_region={{your deployment region}}
```

A list of cloud providers for each region can be found [here](https://docs.confluent.io/cloud/current/clusters/regions.html#cloud-providers-and-regions).
There isn't an online resource for schema registry regions, but you can list and find your region by  
use the following command:
```
confluent schema-registry region list
```

## Docker

You will also need to ensure that you have a docker instance running on your machine.  
A guide for Docker setup can be found [here](https://docs.docker.com/desktop).

## Spinning up the deployment

Once you have completed the setup, run the following setup command:
```
start.sh
```
This will initialise the Confluent Cloud environment, along with all dependencies.  
It will then run the streaming apps from the inital deployment in the blog, ready to start tinkering.

A side note, if you are finding that corporate certificate authorities are causing issue with schema  
registry SSL handshakes, there is also a version of the start script that runs the containers locally
```
start-no-docker.sh
```

Happy building!