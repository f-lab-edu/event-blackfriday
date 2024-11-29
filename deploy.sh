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

if ! command -v docker compose &> /dev/null; then
    log "ERROR: Docker Compose not found"
    exit 1
fi

if [ ! -f ".env" ]; then
    log "ERROR: .env file not found"
    exit 1
fi

log "Checking current deployment status..."
CURRENT_PORT1=$(docker port blackfriday-app1 8080/tcp 2>/dev/null | cut -d ':' -f2 || echo "8080")
CURRENT_PORT2=$(docker port blackfriday-app2 8080/tcp 2>/dev/null | cut -d ':' -f2 || echo "8081")
log "Current ports - APP1: $CURRENT_PORT1, APP2: $CURRENT_PORT2"

if [ "$CURRENT_PORT1" = "8080" ]; then
    sed -i 's/APP1_PORT=8080/APP1_PORT=8082/' .env
else
    sed -i 's/APP1_PORT=8082/APP1_PORT=8080/' .env
fi

if [ "$CURRENT_PORT2" = "8081" ]; then
    sed -i 's/APP2_PORT=8081/APP2_PORT=8083/' .env
else
    sed -i 's/APP2_PORT=8083/APP2_PORT=8081/' .env
fi

log "Starting MySQL..."
docker compose up -d mysql

log "Deploying APP1..."
docker compose up -d --no-deps app1
log "APP1 deployment initiated"

log "Deploying APP2..."
docker compose up -d --no-deps app2
log "APP2 deployment initiated"

log "Configuring Nginx..."
docker compose up -d nginx
reload_nginx

log "Deployment process completed"
