# deploy.sh
#!/bin/bash
set -e

# 매개변수 검증
if [ "$1" != "blue" ] && [ "$1" != "green" ]; then
    echo "Usage: $0 <blue|green>"
    exit 1
fi

# 환경 설정
ENVIRONMENT=$1
case $ENVIRONMENT in
    "blue")
        PORT=8080
        ;;
    "green")
        PORT=8081
        ;;
esac

DOCKER_IMAGE="docker.io/${DOCKER_HUB_USERNAME}/blackfriday-app:${VERSION}"
CONTAINER_NAME="app-${ENVIRONMENT}"

# 배포 함수
deploy() {
    echo "Deploying $ENVIRONMENT environment on port $PORT"

    # 기존 컨테이너 백업
    docker rename ${CONTAINER_NAME} ${CONTAINER_NAME}-old || true

    # 새 컨테이너 실행
    docker-compose -f docker-compose.yml \
        -p ${ENVIRONMENT} \
        up -d \
        --force-recreate

    # 헬스체크
    for i in {1..30}; do
        if curl -s http://localhost:${PORT}/actuator/health | grep "UP"; then
            echo "$ENVIRONMENT deployment successful"
            docker rm -f ${CONTAINER_NAME}-old || true
            exit 0
        fi
        echo "Waiting for application to start... ($i/30)"
        sleep 2
    done

    # 실패시 롤백
    echo "$ENVIRONMENT deployment failed - rolling back"
    docker-compose -f docker-compose.yml -p ${ENVIRONMENT} down
    docker rename ${CONTAINER_NAME}-old ${CONTAINER_NAME}
    docker start ${CONTAINER_NAME}
    exit 1
}

# 메인 실행
deploy
