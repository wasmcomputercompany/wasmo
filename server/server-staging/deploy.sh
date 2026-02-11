#!/bin/bash
set -o errexit

./gradlew server:server-staging:publishImageToLocalRegistry

docker tag wasmo-staging:latest registry.fly.io/wasmo-staging:latest

fly auth docker
docker push registry.fly.io/wasmo-staging:latest

fly deploy server/server-staging
