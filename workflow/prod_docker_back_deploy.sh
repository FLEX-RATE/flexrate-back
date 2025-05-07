#!/bin/bash

# 에러 발생 시 즉시 종료
set -e

echo "=== 프로덕션 환경 인프라 재빌드 시작 ==="

echo "1. Gradle 빌드"
./gradlew clean build -x test

echo "2. .env 파일을 build/libs/로 복사"
cp .env build/libs/

echo "3. 도커 백엔드 이미지를 --no-cache로 재빌드"
docker-compose build --no-cache backend

echo "4. 도커 백엔드 컨테이너를 새로 실행"
docker-compose up -d backend

echo "모든 작업이 완료되었습니다."