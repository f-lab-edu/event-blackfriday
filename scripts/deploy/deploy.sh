#!/bin/bash
set -e
set -x

DOCKER_COMPOSE="docker-compose"
APP_NAME="blackfriday"
DEPLOYMENT_HISTORY="/app/.deployment_history"
NGINX_CONF="./nginx/app.conf"

CURRENT_PORTS=(8080 8081)
NEW_PORTS=(8082 8083)

update_nginx_config() {
    local action=$1
    local port=$2
    local temp_file=$(mktemp)

    case $action in
        "remove")
            sed "/server localhost:$port;/d" $NGINX_CONF > "$temp_file"
            ;;
        "add")
            sed "/upstream app_servers/a\    server localhost:$port;" $NGINX_CONF > "$temp_file"
            ;;
    esac

    mv "$temp_file" $NGINX_CONF
    $DOCKER_COMPOSE exec -T nginx nginx -s reload || true
}

echo "Starting Rolling Deployment..."

$DOCKER_COMPOSE ps > previous_state.txt || true
CURRENT_IMAGE=$(docker ps --filter name=$APP_NAME -q | head -n1 | xargs -I {} docker inspect {} -f '{{.Config.Image}}') || true

NEW_IMAGE=$1
if [ -z "$NEW_IMAGE" ]; then
    echo "Error: No image specified"
    exit 1
fi

echo "$(date '+%Y-%m-%d %H:%M:%S') - Deploying: $NEW_IMAGE" >> "$DEPLOYMENT_HISTORY"

for i in {0..1}; do
    current_port=${CURRENT_PORTS[$i]}
    new_port=${NEW_PORTS[$i]}
    instance="app$((i+1))"

    echo "Updating $instance (Port $current_port -> $new_port)..."

    echo "Deploying new version on port $new_port..."
    export DOCKER_IMAGE=$NEW_IMAGE
    export PORT=$new_port
    $DOCKER_COMPOSE up -d $instance

    sleep 10

    update_nginx_config "add" $new_port

    sleep 5

    update_nginx_config "remove" $current_port

    export PORT=$current_port
    $DOCKER_COMPOSE stop $instance || true

    echo "Successfully updated $instance to port $new_port"
    sleep 5
done

rm -f previous_state.txt
echo "Rolling deployment completed successfully"
exit 0
