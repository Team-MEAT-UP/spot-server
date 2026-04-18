# 부하 테스트 디렉토리

> GCP VM에서 Spring Boot + PostgreSQL + Redis를 Docker로 실행하고, 로컬 MacBook에서 k6로 부하를 발생시키는 구조

## 디렉토리 구조

```
load-test/
├── docker-compose.yml          # GCP VM에서 실행
├── init.sql                    # 테스트 데이터 시드
├── wiremock/                   # WireMock stub 파일 (Scenario 4)
└── scripts/
    ├── common.js               # 공용 설정 (BASE_URL, JWT, 테스트 데이터)
    ├── scenario1-floyd-warshall.js   # Floyd-Warshall O(1) 캐싱
    ├── scenario2-redis-cache.js      # Redis Cache Aside
    ├── scenario3-rate-limiter.js     # Rate Limiter Race Condition
    ├── scenario4-circuit-breaker.js  # Resilience4j Circuit Breaker
    └── scenario5-gzip.js             # Gzip CompressFilter
```

---

## 🚀 빠른 시작

### 1. GCP VM 서버 실행

```bash
# GCP VM에서
cd ~/moisam-server
git pull origin develop

# Spring Boot 이미지 빌드
./gradlew bootJar -x test
docker build -t moisam-server:loadtest .

# 전체 스택 실행
cd load-test
docker compose up -d

# 헬스체크 (변경된 18080 포트)
curl http://localhost:18080/actuator/health
```

### 2. JWT 토큰 발급

OAuth2 카카오 로그인 불가 환경이므로 아래 방법 중 하나 선택:

- **방법 A**: Swagger UI(`http://<GCP-IP>:18080/swagger-ui.html`)에서 테스트 로그인 후 쿠키의 `Access-Token` 복사
- **방법 B**: `common.js`의 `JWT_TOKEN`에 직접 붙여넣기 (24시간 유효)

### 3. 로컬에서 k6 실행

```bash
# Mac 로컬에 k6 설치
brew install k6

# 결과 저장 디렉토리 생성
mkdir -p results

# Before 측정 (18080 포트)
BASE_URL=http://<GCP-IP>:18080 \
  k6 run --out json=results/s1_before.json scripts/scenario1-floyd-warshall.js

# After 측정 (최적화 활성화 상태)
BASE_URL=http://<GCP-IP>:18080 \
  k6 run --out json=results/s1_after.json scripts/scenario1-floyd-warshall.js
```

---

## 📋 시나리오별 Before/After 설정 방법

| 시나리오 | Before 설정 | After 설정 |
|----------|-------------|-----------|
| S1. Floyd-Warshall 캐싱 | `SPRING_CACHE_TYPE=none` 환경변수 추가 후 restart | 기본 설정 |
| S2. Redis Cache Aside | 동일한 캐시 비활성화 | 기본 설정 |
| S3. Rate Limiter | `RedisRateLimiter` Bean 주석 처리 | 기본 설정 |
| S4. Circuit Breaker | WireMock Phase별 교체 | WireMock 정상 응답 |
| S5. Gzip | `CompressFilter` `@Component` 제거 | 기본 설정 |

```bash
# Before: 캐시 비활성화 상태로 재시작
docker compose stop app
docker compose run -e SPRING_CACHE_TYPE=none -e SPRING_PROFILES_ACTIVE=loadtest -d app
```

---

## 📊 결과 확인

```bash
# k6 결과 요약 출력
k6 run --summary-export=results/s1_summary.json scripts/scenario1-floyd-warshall.js

# Redis 히트율 확인 (GCP VM에서)
docker exec $(docker compose ps -q redis) redis-cli INFO stats | grep keyspace_

# 애플리케이션 로그 확인
docker compose logs --tail=100 app
```

---

## ⚠️ 주의사항

- `init.sql`의 테이블명/컬럼명은 실제 JPA 엔티티와 맞게 수정 필요
- `common.js`의 `TEST_DATA.eventIds`와 `init.sql`의 UUID 반드시 동기화
- `ddl-auto: create`이므로 매 `docker compose up` 시 데이터 초기화됨 → 앱 기동 후 시드 데이터 재삽입 필요
- Pinpoint Agent 연동 시 `docker-compose.yml`의 주석 해제 후 Agent 경로 확인
