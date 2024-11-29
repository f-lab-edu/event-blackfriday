#!/bin/bash

DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
DOCKER_COMPOSE="${DOCKER_CONFIG}/cli-plugins/docker-compose"

CURRENT_PORT1=$(docker port blackfriday-app1 8080/tcp | cut -d ':' -f2 || echo "8080")
CURRENT_PORT2=$(docker port blackfriday-app2 8080/tcp | cut -d ':' -f2 || echo "8081")

echo "Current ports - APP1: $CURRENT_PORT1, APP2: $CURRENT_PORT2"

if [ "$CURRENT_PORT1" = "8080" ]; then
    export APP1_PORT=8082
else
    export APP1_PORT=8080
fi

if [ "$CURRENT_PORT2" = "8081" ]; then
    export APP2_PORT=8083
else
    export APP2_PORT=8081
fi

echo "New ports - APP1: $APP1_PORT, APP2: $APP2_PORT"

echo "Deploying app1..."
$DOCKER_COMPOSE up -d --no-deps app1

echo "Deploying app2..."
$DOCKER_COMPOSE up -d --no-deps app2

echo "Deployment completed"
