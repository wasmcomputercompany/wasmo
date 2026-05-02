#!/bin/bash
set -o errexit

./gradlew :os:distributions:sandbox:publishImageToLocalRegistry

docker tag wasmo-sandbox:latest registry.fly.io/wasmo-sandbox:latest

fly auth docker
docker push registry.fly.io/wasmo-sandbox:latest

fly deploy os/distributions/sandbox
