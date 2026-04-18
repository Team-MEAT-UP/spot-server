#!/bin/bash

# 설정: 에러 발생 시 즉시 중단
set -e

echo "============================================"
echo "🚀 MOISAM Load-Test Auto Deployment Starts"
echo "============================================"

# 1. 최신 코드 가져오기
echo "📥 1/5: Pulling latest changes from Git..."
git pull origin feat/load-test

# 2. Gradle 빌드 (JAR 생성)
echo "🏗️ 2/5: Building Spring Boot JAR..."
# 테스트와 문서 생성을 제외하여 빌드 속도를 높입니다.
./gradlew bootJar -x test -x openapi3 -x copyOasToSwagger

# 3. 시스템 및 도커 공간 청소 (용량 확보)
echo "🧹 3/5: Cleaning up system logs and Docker data (Aggressive)..."
sudo apt-get clean
sudo journalctl --vacuum-time=1h
docker system prune -a --volumes -f
df -h

# 4. 도커 이미지 빌드
echo "🐳 4/5: Building Docker Image..."
docker build -t moisam-server:loadtest \
  --build-arg JAR_FILE=build/libs/*.jar \
  -f deploy/Dockerfile .

# 5. 컨테이너 재기동
echo "♻️ 5/5: Restarting Application Container..."
cd load-test
docker compose up -d app

echo "============================================"
echo "✅ Deployment Complete!"
echo "📡 Showing logs (Press Ctrl+C to exit)..."
echo "============================================"

# 로그 출력
docker compose logs -f app
