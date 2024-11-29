#!/bin/bash

set -e

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

cd "$(dirname "$0")/docker"
log "Changed to directory: $(pwd)"

if ! command -v docker compose &> /dev/null; then
    log "ERROR: Docker Compose not found"
    exit 1
fi

log "Checking current container status..."
docker ps -a

CURRENT_PORT1=$(docker port blackfriday-app1 8080/tcp 2>/dev/null | cut -d ':' -f2 || echo "8080")
CURRENT_PORT2=$(docker port blackfriday-app2 8080/tcp 2>/dev/null | cut -d ':' -f2 || echo "8081")

log "Current ports - APP1: $CURRENT_PORT1, APP2: $CURRENT_PORT2"

if [ ! -f ".env" ]; then
    log "ERROR: .env file not found"
    exit 1
fi

log "Starting deployment of app1..."
docker compose up -d --no-deps app1
if [ $? -ne 0 ]; then
    log "ERROR: Failed to deploy app1"
    exit 1
fi

log "Waiting for app1 to start..."
sleep 10

log "Starting deployment of app2..."
docker compose up -d --no-deps app2
if [ $? -ne 0 ]; then
    log "ERROR: Failed to deploy app2"
    exit 1
fi

log "Waiting for app2 to start..."
sleep 10

# 배포 상태 확인
log "Checking deployment status..."
if docker ps | grep -q "blackfriday-app1" && docker ps | grep -q "blackfriday-app2"; then
    log "Deployment completed successfully"
else
    log "ERROR: Deployment verification failed"
    docker ps
    exit 1
fi
