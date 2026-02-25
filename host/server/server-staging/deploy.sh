#!/bin/bash
set -o errexit

./gradlew :host:server:server-staging:publishImageToLocalRegistry

docker tag wasmo-staging:latest registry.fly.io/wasmo-staging:latest

fly auth docker
docker push registry.fly.io/wasmo-staging:latest

fly deploy host/server/server-staging
