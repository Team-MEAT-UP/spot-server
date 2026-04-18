# 🗺️ 모이삼 서버 개발 로드맵

> 개선해야 할 항목을 **난이도 낮음 → 높음** 순으로 정렬한 문서입니다.

## 🟢 낮음

### 2. [Auth] Refresh Token Redis 블랙리스트/화이트리스트 관리

**현재 문제:**
- `reIssueToken()` 에서 Refresh Token 서명(JWT 검증)만 하고, **DB나 Redis에 저장된 토큰과 대조하지 않음**
- 로그아웃 후에도 탈취된 Refresh Token으로 새 Access Token 발급이 가능한 보안 취약점

**개선 목표:**
- 로그인 시 Refresh Token을 Redis에 저장 (`key: userId, value: refreshToken`)
- 재발급 요청 시 Redis에 저장된 토큰과 비교 검증
- 로그아웃 시 Redis에서 해당 토큰 삭제 (블랙리스트 또는 화이트리스트 방식)

---

## 🟡 중간

### 4. [Event] `@Transactional` 클래스 레벨 적용 방식 개선

**현재 문제:**
- `EventService`에 `@Transactional`이 **클래스 레벨**로 적용되어 있어, `GET 조회 메서드`에도 불필요한 트랜잭션이 열림
- `getMeetingPointRoutes()`처럼 무거운 조회 메서드에 ReadOnly 설정이 없어 DB 쓰기 락 낭비 발생 가능

**개선 목표:**
- 클래스 레벨 `@Transactional` 제거
- 각 메서드에 명시적으로 적용 (`@Transactional(readOnly = true)` / `@Transactional`)

---

### 5. [Event] 분산락 개선 및 Redis Prefix (Prod/Stg 공유) 분리

**현재 문제:**
- `EventLockManager`가 `ConcurrentHashMap<UUID, ReentrantLock>` 기반의 **로컬 락**을 사용
- 단일 인스턴스에서는 정상 동작하지만, **AWS ECS 멀티 인스턴스(수평 확장)** 환경에서는 두 서버의 로컬 락이 서로 모름 → 중복 계산 발생 가능
- 추가로 현재 Prod와 Staging이 ElastiCache를 공유할 계획이나, `RedisConfig`의 커스텀 빈으로 인해 환경별 `key-prefix`가 무시되는 상태 → **Staging이 Prod 데이터(특히 Rate Limiter 한도)를 덮어쓰거나 오염시킬 위험**

**개선 목표:**
- Redis 기반 분산락(`Redisson` 등) 으로 교체
- 분산락 도입과 함께 `RedisConfig`, `RedisRateLimiter` 리팩토링하여 Prod/Staging 간의 완벽한 Key Prefix 분리 적용

---

### 6. [Test] 테스트 커버리지 확대

- `MeetingPointCalculator` 통합 테스트 추가
- `SubwayProcessor` 시나리오별 단위 테스트 추가 (Fallback 발동 케이스 포함)
- `EventService` 동시성 테스트 추가

---

### 7. [User] 탈퇴 회원 데이터 처리 정책 명확화

- 현재 `User.withdraw()` 시 Soft Delete (`deletedAt` 세팅) 방식이나, 엔티티 FK(start_point, event)가 남아 있어 완전 삭제 시 오류 가능
- 탈퇴 후 데이터 보관 기간(예: 30일) 및 실물 삭제(`DELETE`) 배치 정책 수립 필요

---

## � 높음

### 8. ⚡ Floyd-Warshall 결과 Redis 캐싱 (콜드 스타트 방지)

**현재 문제:**
- `SubwayPathProcessor`의 `@PostConstruct`에서 O(V^3) 계산을 서버 기동 시마다 실행
- ECS Fargate에서 오토스케일링으로 새 인스턴스가 뜰 때마다 수백 ms 계산 비용 발생

**개선 목표:**
- 최초 계산 후 `shortestTime[][]`, `nextNode[][]` 결과를 Redis에 직렬화해서 저장
- 이후 기동 시 Redis에서 로드하여 계산 Skip
- `SubwayCsvLoader` 재실행 감지 시(데이터 변경)에만 캐시 무효화 및 재계산

**기술 포인트:**
- `int[][]` 직렬화 방식 결정 필요 (e.g. Kryo, Protobuf, 단순 JSON)
- Redis Key 설계: `floyd:shortestTime`, `floyd:nextNode`
- Redis TTL 미설정 (영구 보관), CSVLoader 실행 시에만 `DEL` 처리

---

### 9. ☁️ GCP → AWS 마이그레이션

**프로덕션 서버:**
- `GCP VM` → `AWS ECS Fargate` 전환 (Docker 이미지 기반이므로 적합)
- ALB + ECS Fargate Service 구성

**테스트(개발) 서버:**
- 낮은 사용 빈도 → **AWS Lambda + API Gateway** (요청 시에만 실행)
- Spring Boot Lambda 전환 시 SnapStart 또는 GraalVM Native Image 검토 (Cold Start 방지)

---

### 10. 📦 이미지 데이터 S3 처리 및 경량화

**개선 목표:**
- 프로필 이미지, 장소 이미지 등을 **AWS S3** 에 업로드 후 CloudFront CDN URL만 DB에 저장하는 구조로 변경
- Presigned URL 방식으로 클라이언트가 서버를 우회하여 S3 직접 업로드
- 이미지 경량화: 리사이징 + WebP 변환 (Lambda@Edge 또는 업로드 시 처리)
- `User.profile_image` 컬럼 길이도 VARCHAR(500)으로 함께 수정 (#1 연동)

---

### 11. ⚡ k6 부하 테스트 및 성능 개선 이력서용 작성

**목표:**
- GCP(또는 AWS EC2) 별도 테스트 환경 구성
- k6로 실제 트래픽 시나리오 작성 → 주요 API 부하 테스트

**테스트 대상 API:**
```
POST /api/v1/events/{eventId}/midpoint  ← 가장 무거운 계산
GET  /api/v1/events/{eventId}/routes   ← Redis 캐시 히트율 테스트
```

- 병목 지점 발견 → 개선 → Before/After 수치 비교
- TPS, P95 응답속도, Error Rate 등 정량 지표로 이력서에 기록

---

### 12. 🔐 Spring Security 전반 개선

| 점검 항목 | 내용 |
|-----------|------|
| CORS 정책 | 운영 환경 origin whitelist 재검토 |
| 민감 정보 로깅 | 이메일, Access Token이 로그에 찍히지 않도록 필터 처리 |
| JWT 알고리즘 | 현재 알고리즘 강도 점검 (HS256 → RS256 고려) |
| Rate Limiting | `/auth/**` 엔드포인트에 Bucket4j 또는 Spring Cloud Gateway Rate Limit 적용 |
| HttpOnly Cookie | 현재 Cookie 설정에서 `SameSite=Strict` 적용 여부 확인 |

---

### 13. 🔐 GitHub Actions 배포 보안 강화 (AWS OIDC 전환)

**개선 목표:**
- GitHub Secrets에 영구적인 AWS Access Key를 저장하지 않고, OIDC를 기반으로 단기 임시 토큰 인증 방식으로 전환하여 보안 위험을 근본적으로 제거

**진행 과정 (AWS IAM 콘솔 설정):**
1. **OIDC Provider 생성**: `token.actions.githubusercontent.com` 접속 허용 (Audience: `sts.amazonaws.com`)
2. **IAM Role 정의**: Web Identity 기반 롤을 생성하고, **신뢰 권한 탭(Trust relationships)에 `StringLike` 조건으로 `repo:[사용자아이디]/moisam-server:*` 를 반드시 추가**하여 타인의 접근 차단
3. **배포 권한 연결**: 기존 GitHub Actions에 쓰던 정책(ECR Push, ECS Update 등)을 본 Role에 부여
4. **GitHub Secrets 설정**: 기존 `AWS_ACCESS_KEY_ID`와 `AWS_SECRET_ACCESS_KEY`는 삭제하고 방금 만든 Role의 ARN을 `AWS_DEPLOY_ROLE_ARN`으로 저장 후 `workflow.yml` 수정

---

> **마지막 업데이트:** 2026-04-02
> **작성자:** 모이삼 백엔드팀
