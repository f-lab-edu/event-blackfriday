#!/bin/bash

set -e

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

error_exit() {
    log "ERROR: $1"
    exit 1
}

reload_nginx() {
    log "Nginx 설정을 리로드하는 중..."
    docker compose exec -T nginx nginx -s reload || log "Warning: Nginx 리로드 실패"
}

log "롤링 업데이트 배포 스크립트 시작"

cd "$(dirname "$0")/docker" || error_exit "docker 디렉토리로 이동 실패"
log "작업 디렉토리: $(pwd)"

if [ -f ".env" ]; then
    log ".env 파일에서 환경 변수 로드 중..."
    set -a
    source .env
    set +a
    log "환경 변수 로드 완료"
else
    error_exit ".env 파일을 찾을 수 없습니다"
fi

required_vars=("MYSQL_HOST" "MYSQL_DATABASE" "MYSQL_USER" "MYSQL_PASSWORD" "DOCKER_IMAGE" "REDIS_PASSWORD")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        error_exit "필수 환경 변수 '$var'가 설정되지 않았습니다."
    fi
done

log "필수 환경 변수 확인 완료"

CURRENT_PORT1=$(docker port blackfriday-app1 8080/tcp 2>/dev/null | cut -d ':' -f2 || echo "8080")
CURRENT_PORT2=$(docker port blackfriday-app2 8080/tcp 2>/dev/null | cut -d ':' -f2 || echo "8081")
log "현재 포트 - APP1: $CURRENT_PORT1, APP2: $CURRENT_PORT2"

if [ "$CURRENT_PORT1" = "8082" ]; then
    NEW_PORT1="8080"
else
    NEW_PORT1="8082"
fi
log "롤링 업데이트 1/2: APP1을 포트 $NEW_PORT1로 업데이트 중..."
export APP1_PORT=$NEW_PORT1

docker compose up -d --no-deps app1 || error_exit "APP1 업데이트 실패"

log "APP1 헬스체크 대기 중 (서비스 가용성 확인)..."
sleep 30

if docker ps | grep -q "blackfriday-app1"; then
    log "롤링 업데이트 2/2: APP1이 정상 작동 확인됨, APP2 업데이트 진행..."

    if [ "$CURRENT_PORT2" = "8083" ]; then
        NEW_PORT2="8081"
    else
        NEW_PORT2="8083"
    fi
    log "APP2를 포트 $NEW_PORT2로 업데이트 중..."
    export APP2_PORT=$NEW_PORT2

    docker compose up -d --no-deps app2 || error_exit "APP2 업데이트 실패"

    log "APP2 헬스체크 대기 중..."
    sleep 30
else
    log "경고: APP1이 정상적으로 실행되지 않았습니다. 롤링 업데이트를 중단합니다."
    error_exit "롤링 업데이트 실패: 첫 번째 인스턴스가 정상적으로 시작되지 않음"
fi

log "로드 밸런서 설정 업데이트 중..."
docker compose up -d nginx || error_exit "Nginx 업데이트 실패"
reload_nginx

log "불필요한 리소스 정리 중..."
docker compose up -d --remove-orphans || log "고아 컨테이너 정리 실패 (무시)"

log "롤링 업데이트 배포 완료 - 두 인스턴스 모두 새 버전으로 업데이트됨"
exit 0
