#!/bin/bash

# 에러 발생 시 즉시 종료
set -e

echo "=== 개발환경 인프라 및 로컬 백엔드 시작 ==="

echo "1. 백엔드 제외 도커 이미지 실행"
docker-compose up -d mysql redis logstash elasticsearch kibana filebeat

echo "2. 로컬 환경 백엔드 실행"
./gradlew bootRun