#!/bin/bash

set -e

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

reload_nginx() {
    log "Reloading Nginx configuration..."
    docker compose exec -T nginx nginx -s reload
}

cd "$(dirname "$0")/docker"
log "Working directory: $(pwd)"

CURRENT_PORT1=$(docker port blackfriday-app1 8080/tcp 2>/dev/null | cut -d ':' -f2 || echo "8080")
CURRENT_PORT2=$(docker port blackfriday-app2 8080/tcp 2>/dev/null | cut -d ':' -f2 || echo "8081")
log "Current ports - APP1: $CURRENT_PORT1, APP2: $CURRENT_PORT2"

if [ "$CURRENT_PORT1" = "8082" ]; then
    NEW_PORT1="8080"
else
    NEW_PORT1="8082"
fi
log "Updating APP1 to port $NEW_PORT1..."
export APP1_PORT=$NEW_PORT1
docker compose up -d --no-deps app1
log "Waiting for APP1 health check..."
sleep 30

if docker ps | grep -q "blackfriday-app1"; then
    if [ "$CURRENT_PORT2" = "8083" ]; then
        NEW_PORT2="8081"
    else
        NEW_PORT2="8083"
    fi
    log "Updating APP2 to port $NEW_PORT2..."
    export APP2_PORT=$NEW_PORT2
    docker compose up -d --no-deps app2
    log "Waiting for APP2 health check..."
    sleep 30
fi

log "Updating Nginx configuration..."
docker compose up -d nginx
reload_nginx

log "Deployment process completed"
