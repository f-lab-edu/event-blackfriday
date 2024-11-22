#!/bin/bash
set -e

# 환경 설정
ENVIRONMENT=$1
case $ENVIRONMENT in
    "blue")
        CONTAINER_NAME="app-blue"
        NGINX_PORT=80  # 또는 실제 사용할 포트
        ;;
    "green")
        CONTAINER_NAME="app-green"
        NGINX_PORT=81  # 또는 실제 사용할 포트
        ;;
    *)
        echo "Usage: $0 <blue|green>"
        exit 1
        ;;
esac

# 배포
echo "Deploying $ENVIRONMENT environment..."
CONTAINER_NAME=$CONTAINER_NAME \
NGINX_PORT=$NGINX_PORT \
DOCKER_IMAGE=$DOCKER_IMAGE \
MYSQL_DATABASE=$MYSQL_DATABASE \
MYSQL_USER=$MYSQL_USER \
MYSQL_PASSWORD=$MYSQL_PASSWORD \
docker-compose up -d

# Health check
echo "Running health check..."
for i in {1..30}; do
    if curl -s http://localhost:$NGINX_PORT/health | grep -q 'OK'; then
        echo "$ENVIRONMENT deployment successful"
        exit 0
    fi
    echo "Waiting for application to start... ($i/30)"
    sleep 2
done

echo "$ENVIRONMENT deployment failed"
exit 1
