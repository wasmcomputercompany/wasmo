#!/bin/bash
set -o errexit

./gradlew :os:distributions:hosted:publishImageToLocalRegistry

docker tag wasmo-hosted:latest registry.fly.io/wasmo-hosted:latest

fly auth docker
docker push registry.fly.io/wasmo-hosted:latest

fly deploy os/distributions/hosted
