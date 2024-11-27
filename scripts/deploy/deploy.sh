#!/bin/bash
set -e
set -x

DOCKER_COMPOSE="docker-compose"
APP_NAME="blackfriday"
DEPLOYMENT_HISTORY="/app/.deployment_history"
NGINX_CONF="../docker/nginx/app.conf"

CURRENT_PORTS=(8080 8081)
NEW_PORTS=(8082 8083)

echo "Starting Rolling Deployment..."

cd /app

$DOCKER_COMPOSE ps > previous_state.txt || true
CURRENT_IMAGE=$(docker ps --filter name=$APP_NAME -q | head -n1 | xargs -I {} docker inspect {} -f '{{.Config.Image}}') || true

if [ "$1" == "rollback" ]; then
    if [ -f previous_image.txt ]; then
        NEW_IMAGE=$(cat previous_image.txt)
    else
        echo "No previous image found for rollback."
        exit 1
    fi
else
    NEW_IMAGE=$1
    echo "$CURRENT_IMAGE" > previous_image.txt
fi

if [ -z "$NEW_IMAGE" ]; then
    echo "Error: No image specified"
    exit 1
fi

echo "$(date '+%Y-%m-%d %H:%M:%S') - Deploying: $NEW_IMAGE" >> "$DEPLOYMENT_HISTORY"

for i in {0..1}; do
    current_port=${CURRENT_PORTS[$i]}
    new_port=${NEW_PORTS[$i]}
    service_name="app$((i+1))"

    echo "Updating $service_name (Port $current_port -> $new_port)..."

    export DOCKER_IMAGE=$NEW_IMAGE
    export PORT1=$new_port
    export PORT2=$new_port

    $DOCKER_COMPOSE up -d $service_name

    echo "Waiting for $service_name to be ready..."
    sleep 10

    if [ -f "$NGINX_CONF" ]; then
        update_nginx_config "add" $new_port
        sleep 5
        update_nginx_config "remove" $current_port
    fi

    echo "Successfully updated $service_name to port $new_port"
done

# 정리
rm -f previous_state.txt
echo "Rolling deployment completed successfully"
exit 0
