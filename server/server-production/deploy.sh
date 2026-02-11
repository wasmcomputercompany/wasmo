#!/bin/bash
set -o errexit

./gradlew server:server-production:publishImageToLocalRegistry

docker tag wasmo-production:latest registry.fly.io/wasmo-production:latest

fly auth docker
docker push registry.fly.io/wasmo-production:latest

fly deploy server/server-production
