# GCP 기반 부하 테스트 계획 (Load Test Plan)

> **목적**: 각 성능 최적화 항목의 효과를 Before/After 수치로 증명하기 위한 정밀 부하 테스트 계획서

---

## 📌 테스트 환경 구성

### 아키텍처

```
  k6                        →      Spring Boot App  (:18080)
                                   ↕
                                   PostgreSQL (:5432)
                                   Redis (:6379)
                                   Pinpoint Agent (JVM)
                                   Pinpoint Web (GCP VM :8080)
```

### 구성 선택 이유

| 항목 | 이유 |
|------|------|
| **GCP에 서버 직접 배포** | AWS ECS 스테이징 EC2는 t4g.micro(1GB)라 부하 테스트 중 OOM 위험. GCP c2-standard-4(16GB)에서 여유롭게 측정 |
| **Docker Compose로 자급자족** | AWS RDS/ElastiCache와 크로스 클라우드 연결 없이 GCP 내에서 완결. 네트워크 변수 제거 |
| **k6는 로컬 실행** | 부하 발생기가 단순하므로 로컬 MacBook으로 충분. GCP VM 자원은 서버에 집중 |
| **Pinpoint APM 연동** | 메서드 레벨 트레이스로 Before/After 병목 지점 시각화. CloudWatch보다 상세한 분석 가능 |
| **빈 DB로 시작** | AWS RDS 덤프 없이 DDL 자동 생성 + 테스트 데이터 시드로 환경 구성 단순화 |

### GCP 인스턴스 스펙

| 항목 | 값 |
|------|-----|
| 머신 타입 | `c2-standard-4` (4vCPU, 16GB RAM) |
| OS | Ubuntu 22.04 LTS |
| 위치 | `asia-northeast3` (서울) |
| 설치 도구 | Docker, Docker Compose, Git, OpenJDK 21 |

---

## 📐 공통 측정 지표 (Metrics)

| 지표 | 설명 | 허용 기준 |
|------|------|-----------|
| `p95 latency` | 상위 95번째 응답 시간 | < 2,000ms |
| `p99 latency` | 상위 99번째 응답 시간 | < 5,000ms |
| `Error Rate` | HTTP 5xx 비율 | < 1% |
| `Throughput (RPS)` | 초당 처리 요청 수 | 목표 TPS 대비 ≥ 90% |
| `CPU / Memory` | ECS 컨테이너 자원 사용률 | CPU < 80% |
| `DB Active Sessions` | RDS 활성 커넥션 수 | Max Connections 미만 |
| `Redis Hit Rate` | 캐시 히트율 | ≥ 80% |

---

## 🧪 테스트 시나리오별 Before / After 계획

---

### Scenario 1: 중간지점 후보군 탐색 알고리즘 (Floyd-Warshall O(1) 캐싱)

#### 테스트 대상 API
```
POST /api/events/{eventId}/meeting-points
```

#### 핵심 검증 포인트
- **Before**: Floyd-Warshall 결과를 매 요청마다 재계산하거나, DB에서 전체 경로 테이블 스캔
- **After**: 사전에 계산된 경로 데이터를 Redis에서 O(1) 조회

#### 부하 시나리오
```js
// k6 script 예시
stages: [
  { duration: '1m', target: 10 },   // Warm-up
  { duration: '3m', target: 50 },   // 일반 부하
  { duration: '2m', target: 100 },  // 스파이크 부하
  { duration: '1m', target: 0 },    // Cool-down
]
```

#### 측정 항목

| 항목 | Before (재계산) | After (O(1) 캐싱) | 목표 개선율 |
|------|---------------|------------------|------------|
| p95 latency | ? ms | ? ms | ≥ 50% ↓ |
| p99 latency | ? ms | ? ms | ≥ 50% ↓ |
| DB Query 횟수/req | ? 회 | ? 회 | - |
| Redis Hit Rate | - | ? % | ≥ 80% |
| Max Throughput (RPS) | ? | ? | ≥ 2x ↑ |

> **Before 환경 재현 방법**: Redis 캐시를 비활성화(`spring.cache.type=none`)한 상태에서 측정 → 캐시 활성화 후 재측정

---

### Scenario 2: Redis 캐싱 (Cache Aside / Write Around 패턴)

#### 테스트 대상 API
```
GET /api/events/{eventId}/routes?stationId={stationId}
```
- ODsay 외부 API 경로 조회 (캐싱 대상)

#### 핵심 검증 포인트
- **Before**: 동일 출발지-도착지 요청마다 ODsay API 외부 호출 발생
- **After**: Redis Cache Hit 시 외부 API 미호출, 응답 시간 단축

#### 부하 시나리오

반복 요청 비율을 의도적으로 높게 설정 (Cache 효과 극대화):
```js
// 동일 파라미터 반복 비율 70%, 신규 파라미터 30%
```

#### 측정 항목

| 항목 | Before (캐시 없음) | After (캐시 적용) | 목표 개선율 |
|------|-----------------|-----------------|------------|
| p95 latency | ? ms | ? ms | ≥ 60% ↓ |
| 외부 API 호출 횟수 | 요청 수 = 호출 수 | 캐시 히트분 제외 | ≥ 70% ↓ |
| Redis Hit Rate | - | ? % | ≥ 70% |
| Error Rate (외부 API 장애 시) | 높음 | 낮음 (캐시 fallback) | < 1% |

> **측정 방법**: Redis `INFO stats`의 `keyspace_hits` / `keyspace_misses` 비율 확인

---

### Scenario 3: Rate Limiter (Lua Script 기반 원자적 처리)

#### 테스트 대상
- ODsay / Kakao API 일일 호출 한도 Rate Limit 동작 검증
- Discord Webhook 알림 발송 시점 확인

#### 핵심 검증 포인트
- **Before**: Rate Limit 없이 외부 API 호출 → 한도 초과 시 서비스 장애
- **After**: Lua Script로 원자적 카운팅 → 한도 임박/초과 시 Fallback 처리 + Discord 알림

#### 부하 시나리오
```js
// 일일 호출 한도의 90%, 100%, 110% 수준에서 동시 다발적 요청 발생
// 멀티 스레드 환경에서 Race Condition 발생 여부 확인
stages: [
  { duration: '30s', target: 30 },  // 한도 90% 수준 도달
  { duration: '30s', target: 60 },  // 한도 초과 유도
]
```

#### 측정 항목

| 항목 | Before | After | 목표 |
|------|--------|-------|------|
| 한도 초과 시 Error Rate | ~100% | < 1% (Fallback) | Graceful Degradation |
| Race Condition 발생 여부 | 발생 가능 | 미발생 | 0건 |
| Discord 알림 수신 시간 | - | 이벤트 발생 후 ? ms | < 5s |
| 임박/초과 알림 정확도 | - | ? % | 100% |

---

### Scenario 4: Resilience4j (Retry / Circuit Breaker / Timeout)

#### 테스트 대상
- ODsay / Kakao API 고의 지연 또는 장애 시뮬레이션

#### 핵심 검증 포인트
- **Before**: 외부 API 장애 시 요청 Thread가 블로킹되어 서버 전체 Hang
- **After**: Timeout(1.5~2s) → Retry(3회) → Circuit Breaker Open → Fallback 반환

#### 부하 시나리오

```js
// 외부 API Mock 서버를 구성하여 응답 지연 시뮬레이션
// Phase 1: 정상 응답 (2s 내) → Baseline 측정
// Phase 2: 5s 지연 응답 주입 → Timeout + Retry 동작 확인
// Phase 3: 100% 오류 응답 → Circuit Breaker Open 전환 확인
```

> **외부 API 장애 주입 방법**: WireMock 또는 Toxiproxy를 GCP에 별도 배포하여 지연 시뮬레이션

#### 측정 항목

| 항목 | Before | After | 목표 |
|------|--------|-------|------|
| 외부 API 5s 지연 시 p99 | 5,000ms+ (블로킹) | < 3,000ms (Timeout 적용) | ≥ 40% ↓ |
| 장애 전파율 (서버 Hang) | 높음 | 0% (Circuit Open) | 0% |
| Circuit Breaker 전환 시간 | - | ? ms | < 10s |
| Fallback 응답 성공률 | 0% | ? % | > 99% |
| 장애 복구 후 Half-Open 전환 | - | 자동 복구 | 확인 |

---

### Scenario 5: Gzip 커스텀 CompressFilter

#### 테스트 대상 API
```
POST /api/events/{eventId}/meeting-points
```
- 대용량 경로 JSON 응답 반환

#### 핵심 검증 포인트
- **Before**: 압축 없이 대용량 JSON 그대로 전송
- **After**: Gzip 압축으로 70~80% 페이로드 감소

#### 부하 시나리오
```js
// Accept-Encoding: gzip 헤더 포함 요청
// 응답 크기(Content-Length) 및 전송 시간 측정
stages: [
  { duration: '2m', target: 50 },
  { duration: '3m', target: 100 },
]
```

#### 측정 항목

| 항목 | Before (압축 없음) | After (Gzip 적용) | 목표 개선율 |
|------|-----------------|-----------------|------------|
| 응답 페이로드 크기 (KB) | ? KB | ? KB | ≥ 70% ↓ |
| p95 latency | ? ms | ? ms | ≥ 30% ↓ |
| 네트워크 대역폭 사용량 | ? MB/s | ? MB/s | ≥ 70% ↓ |
| 서버 CPU 사용률 | ? % | ? % (압축 오버헤드) | +5% 이하 |

> **Gzip 비활성화 방법**: `CompressFilter`를 Bean 등록에서 제외하거나 `Content-Encoding` 헤더 없이 요청

---

## 🗓️ 테스트 실행 순서 (권장)

```
[환경 구성]
Step 1. GCP VM에 Docker + Java 21 설치 (셋업 가이드 참고)
Step 2. application-loadtest.yml 추가 후 Spring Boot 이미지 빌드
Step 3. docker-compose.yml로 Spring Boot + PostgreSQL + Redis 실행
Step 4. Pinpoint Agent 연동 (설치 완료)
Step 5. GCP 방화벽 규칙 추가 (로컬 IP → 18080, 8080 허용)
Step 6. 로컬에서 curl로 헬스체크 확인 (18080 포트)

[Before 측정]
Step 7-A. 각 최적화 기능 비활성화 상태로 docker compose restart
Step 7-B. 로컬에서 시나리오별 k6 스크립트 실행 (2분 워밍업 포함)
Step 7-C. 측정값 기록 (p95/p99, RPS, Error Rate, Pinpoint 트레이스)

[After 측정]
Step 8-A. 각 최적화 기능 활성화 상태로 docker compose restart
Step 8-B. 동일 k6 스크립트 실행
Step 8-C. 측정값 기록 및 Before 대비 비교

Step 9. 결과 정리 및 portfolio.md 반영
```

---

## 🛠️ GCP VM 셋업 가이드

### Step 1. 기본 환경 설치 (GCP VM)

```bash
# Docker 설치
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt-get update && sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker $USER && newgrp docker

# Java 21 설치 (Gradle 빌드용)
sudo apt-get install -y openjdk-21-jdk
java -version
```

### Step 2. Spring Boot 이미지 빌드 (GCP VM)

```bash
# 레포 클론
git clone https://github.com/<your-org>/moisam-server.git
cd moisam-server

# 테스트 제외하고 JAR 빌드
./gradlew bootJar -x test

# Dockerfile은 deploy/ 폴더에 위치. 프로젝트 루트에서 build-arg로 JAR 경로 지정
docker build \
  -f deploy/Dockerfile \
  --build-arg JAR_FILE=build/libs/*.jar \
  -t moisam-server:loadtest \
  .
```

> **layered jar 방식**: Dockerfile이 `layertools`로 JAR를 분해하므로 일반적인 `-jar` 방식보다 콘테이너 기동이 빠르고 이미지 레이어 캐시면에서 유리함

### Step 3. application-loadtest.yml 추가

`src/main/resources/application-loadtest.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/moisam
    username: moisam
    password: moisam
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2

  data:
    redis:
      host: redis
      port: 6379
      key-prefix: "loadtest:"

  jpa:
    hibernate:
      ddl-auto: create   # 빈 DB → 테이블 자동 생성
    show-sql: false
    properties:
      hibernate:
        format_sql: false

logging:
  level:
    root: WARN
    com.meetup: INFO
```

> `ddl-auto: create`는 부하 테스트 전용. 매 실행 시 테이블이 초기화됨.

### Step 4. docker-compose.yml (GCP VM)

```yaml
version: '3.8'

services:
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: moisam
      POSTGRES_USER: moisam
      POSTGRES_PASSWORD: moisam
    ports:
      - "5432:5432"
    volumes:
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql   # 테스트 데이터 시드
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U moisam"]
      interval: 5s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes

  app:
    image: moisam-server:loadtest
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: loadtest
      # 외부 API 키 등 필요 시 추가
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_started
    # Pinpoint Agent 연동 시 아래 주석 해제
    # volumes:
    #   - /opt/pinpoint-agent:/pinpoint-agent
    # entrypoint: ["java", "-javaagent:/pinpoint-agent/pinpoint-bootstrap.jar",
    #              "-Dpinpoint.agentId=moisam-loadtest",
    #              "-Dpinpoint.applicationName=moisam-server",
    #              "-jar", "/app/app.jar"]
```

### Step 5. 서버 실행

```bash
# GCP VM에서
docker compose up -d

# 헬스체크
curl http://localhost:8080/actuator/health
# 응답: {"status":"UP"}

# 로그 확인
docker compose logs -f app
```

### Step 6. GCP 방화벽 규칙 (외부 접근용)

```bash
# GCP Console → VPC → 방화벽 규칙 추가
# 또는 gcloud CLI
gcloud compute firewall-rules create allow-moisam-loadtest \
  --allow tcp:8080 \
  --source-ranges=<로컬-IP>/32 \
  --description="Local k6 access to loadtest server"
```

### Step 7. 로컬에서 k6 설치 및 실행

```bash
# Mac 로컬
brew install k6

# 연결 확인
curl http://<GCP-외부-IP>:8080/actuator/health

# k6 실행
k6 run \
  --out json=results/scenario1_before.json \
  scripts/scenario1-meeting-points.js
```

### WireMock (외부 API 장애 시뮬레이션 — Scenario 4용)

```bash
# GCP VM에서 추가 실행
docker run -d -p 9090:8080 wiremock/wiremock:latest
# Spring Boot의 ODsay/Kakao API 엔드포인트를 WireMock으로 오버라이드
```

---

## 📊 결과 기록 템플릿

> 테스트 완료 후 아래 표를 채워서 portfolio.md에 반영

| 최적화 항목 | 측정 지표 | Before | After | 개선율 |
|------------|----------|--------|-------|--------|
| Floyd-Warshall O(1) 캐싱 | p95 latency | - ms | - ms | - % |
| Floyd-Warshall O(1) 캐싱 | Max RPS | - | - | - % |
| Redis 캐싱 | p95 latency | - ms | - ms | - % |
| Redis 캐싱 | 외부 API 호출 감소 | - | - | - % |
| Rate Limiter | 한도 초과 Error Rate | ~100% | - % | - |
| Resilience4j | 장애 전파율 | - % | 0% | - |
| Resilience4j | p99 (장애 시) | - ms | - ms | - % |
| Gzip CompressFilter | 응답 크기 | - KB | - KB | ~75% ↓ |
| Gzip CompressFilter | p95 latency | - ms | - ms | - % |

---

## ⚠️ 주의사항

### 환경 관련

- **GCP VM 전용 환경**: 프로덕션 DB/Redis(AWS)에 절대 부하를 주지 않음. GCP Docker 내부에서 완결
- **외부 API는 WireMock 대체**: ODsay/Kakao API에 실제 부하 시 일일 한도 소진 위험 → `docker-compose.yml`의 WireMock 서비스로 대체
- **ddl-auto: create 주의**: `application-loadtest.yml`의 `ddl-auto: create`는 매 `docker compose up` 시 테이블 초기화. 앱 기동 후 `init.sql` 수동 재삽입 필요

### 빌드 관련

- **Spring Boot 이미지**: ECR(AWS 전용)은 GCP에서 접근 불가 → GCP VM에서 `git clone` 후 직접 빌드
- **ARM64 주의**: GCP VM이 x86 기반이면 `docker build` 시 `--platform linux/amd64` 명시. `Dockerfile`에 멀티플랫폼 빌드 설정 확인
- **Pinpoint Agent**: `docker-compose.yml`의 주석 처리된 `volumes`, `entrypoint` 블록을 Pinpoint 설치 이후 활성화

### 측정 관련

- **워밍업 필수**: JVM JIT 컴파일 전 초기 응답은 느림. 최소 **2분 워밍업 후** 측정값 기록
- **환경 일관성**: Before/After 모두 동일 GCP VM 스펙, 동일 Docker 이미지 사용
- **init.sql 동기화**: `common.js`의 `TEST_DATA.eventIds` UUID와 `init.sql`의 UUID가 반드시 일치해야 함
- **Redis 히트율 별도 확인**: k6 메트릭만으로는 부족 → GCP VM에서 직접 확인

```bash
# Redis 캐시 히트율 확인 (GCP VM에서)
docker exec $(docker compose ps -q redis) redis-cli INFO stats | grep keyspace_
# keyspace_hits / (keyspace_hits + keyspace_misses) = 히트율
```

---

## ▶️ k6 실행 명령어 모음

```bash
# 0. 로컬에 k6 설치
brew install k6

# 1. GCP 서버 헬스체크
curl http://<GCP-외부-IP>:8080/actuator/health

# 2. 결과 디렉토리 생성
mkdir -p load-test/results

# ── Before 측정 (최적화 비활성화 상태로 서버 재시작 후) ──
BASE_URL=http://<GCP-외부-IP>:8080 k6 run --out json=load-test/results/s1_before.json load-test/scripts/scenario1-floyd-warshall.js
BASE_URL=http://<GCP-외부-IP>:8080 k6 run --out json=load-test/results/s2_before.json load-test/scripts/scenario2-redis-cache.js
BASE_URL=http://<GCP-외부-IP>:8080 k6 run --out json=load-test/results/s3_before.json load-test/scripts/scenario3-rate-limiter.js
BASE_URL=http://<GCP-외부-IP>:8080 PHASE=1 k6 run --out json=load-test/results/s4_phase1.json load-test/scripts/scenario4-circuit-breaker.js
BASE_URL=http://<GCP-외부-IP>:8080 k6 run --out json=load-test/results/s5_before.json load-test/scripts/scenario5-gzip.js

# ── After 측정 (최적화 활성화 상태로 서버 재시작 후) ──
BASE_URL=http://<GCP-외부-IP>:8080 k6 run --out json=load-test/results/s1_after.json  load-test/scripts/scenario1-floyd-warshall.js
BASE_URL=http://<GCP-외부-IP>:8080 k6 run --out json=load-test/results/s2_after.json  load-test/scripts/scenario2-redis-cache.js
BASE_URL=http://<GCP-외부-IP>:8080 k6 run --out json=load-test/results/s3_after.json  load-test/scripts/scenario3-rate-limiter.js
BASE_URL=http://<GCP-외부-IP>:8080 k6 run --out json=load-test/results/s5_after.json  load-test/scripts/scenario5-gzip.js
```

---

*마지막 업데이트: 2026-04-16*
