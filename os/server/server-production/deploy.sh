#!/bin/bash
set -o errexit

./gradlew :os:server:server-production:publishImageToLocalRegistry

docker tag wasmo-production:latest registry.fly.io/wasmo-production:latest

fly auth docker
docker push registry.fly.io/wasmo-production:latest

fly deploy host/server/server-production
