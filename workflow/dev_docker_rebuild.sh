#!/bin/bash

# 에러 발생 시 즉시 종료
set -e

echo "=== 개발환경 인프라 재빌드 시작 ==="

echo "1. 백엔드 제외 도커 이미지 재빌드"
docker-compose build --no-cache mysql redis logstash elasticsearch kibana filebeat

echo "2. 백엔드 제외 도커 컨테이너 실행"
docker-compose up -d mysql redis logstash elasticsearch kibana filebeat

echo "재빌드 후 실행이 완료되었습니다. 로컬 환경 백엔드를 실행합니다."

echo "3. 로컬 환경 백엔드 실행"
./gradlew bootRun