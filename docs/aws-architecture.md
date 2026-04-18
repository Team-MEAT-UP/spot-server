# AWS 마이그레이션 최종 아키텍처

## 전체 구성도

```
                       ┌──────────────────────────────────────────────────────┐
                       │  VPC (ap-northeast-2)                                │
                       │                                                      │
                       │  ┌─ Public Subnet ──────────────────────────────┐   │
  [Vercel]  ──┐        │  │                                              │   │
  [Users]   ──┼──▶ [ALB] ──▶  [ECS Fargate]     [EC2]                  │   │
  [GitHub]  ──┘   (ACM) │  │    (Prod)           (Staging + Bastion) ───┼─┐ │
   Actions     │        │  └─────────────────────────────────────────────┘ │ │
               │        │                                                   │ │
          (SSH deploy)  │  ┌─ Private Subnet ─────────────────────────────┐ │ │
               └────────┼──▶  [ElastiCache]      [RDS]                   │◀┘ │
                        │  │    (Redis)           (PostgreSQL)             │   │
                        │  └──────────────────────────────────────────────┘   │
                        └──────────────────────────────────────────────────────┘

  ┌─ AWS Services ──────────────────────────────┐
  │  ECR · S3 · SSM · CloudWatch · Discord      │
  └─────────────────────────────────────────────┘
```

---

## 아키텍처 의사결정

### Fargate vs EC2 Launch Type

ECS는 컨테이너 오케스트레이션 서비스이고, 컨테이너를 실행하는 방식이 두 가지야.

| | ECS Fargate | ECS EC2 |
|---|---|---|
| 서버 관리 | AWS가 관리 (신경 쓸 필요 없음) | 직접 EC2 인스턴스 관리 |
| 스케일링 단위 | **컨테이너(Task)** | EC2 인스턴스 + 컨테이너 |
| 스케일링 속도 | 30~60초 | EC2 부팅 2~5분 + 컨테이너 시작 |
| 비용 구조 | 사용한 vCPU/메모리만큼 과금 | EC2 인스턴스 상시 과금 |
| 운영 부담 | 낮음 | 높음 (패치, 용량 관리 등) |
| 적합한 트래픽 | 변동이 크거나 운영 최소화 | 고정 트래픽, 높은 상시 이용률 |

**ECS EC2를 고려한 이유**

Redis를 ElastiCache 대신 EC2 위의 컨테이너로 띄워 $12/월을 절감하는 방안을 검토했다.

```
ECS EC2 구조 (검토안)
  EC2 인스턴스
  ├─ Redis ECS Service (ECS Service Connect으로 DNS 제공)
  └─ Spring Boot ECS Service (Auto Scaling)
       └─ redis.moisam.local:6379 로 공유 Redis 접속
```

**Fargate를 유지한 이유**

- 트래픽 패턴이 `평소 idle → 간헐적 스파이크`이므로 EC2 고정 비용보다 Fargate 종량제가 유리
- ECS EC2 Auto Scaling은 2-tier (EC2 Auto Scaling Group + ECS Task 스케일링)라 구성 복잡도가 높고 스케일링이 느림
- 비용 절감액이 $12/월 수준으로 복잡도 대비 효과가 작음

**결론: 트래픽이 예측 불가능하고 운영 인력이 없는 소규모 팀 → Fargate 유지**

### Staging 서버를 EC2로 구성한 이유

초기에는 Fargate Spot을 고려했으나, GitHub Actions 환경에서 SSH 방식으로 컨테이너 레벨에 직접 손쉽게 배포하고 세밀한 제어(예: Docker 환경)를 하기 위해 **단일 EC2 인스턴스**에 도커 컨테이너를 직접 띄우는 방식으로 최종 결정했다.

- 운영(Prod): 무중단 스케일아웃이 중요한 Fargate
- 테스트(Staging): 단일 EC2에 Docker 기반 배포로 인프라 비용 및 배포 파이프라인 단순화. ALB의 Staging Target Group은 이 EC2 인스턴스의 8080 포트를 직접 바라보도록 설정.

### ElastiCache 유지 결정

Redis 사용처가 두 곳이며, Auto Scaling 시 공유 Redis가 필수인 이유가 있다.

| 사용처 | 역할 | 사이드카 시 문제 |
|---|---|---|
| `CachedRouteRepository` | 경로 계산 결과 캐시 | Task별 캐시 분리 → 캐시 미스 증가, 외부 API 호출 증가 |
| `RedisRateLimiter` | ODsay / Kakao Mobility 일일 API 한도 관리 | **Task별 독립 카운터 → 실제 호출량이 Task 수배로 증가, API 한도 초과 위험** |

Task 3개로 스케일 아웃 시 Rate Limiter가 공유되지 않으면 의도한 일일 한도의 3배까지 API를 호출할 수 있다. ElastiCache $12/월은 이 리스크에 대한 보험료다.

> Auth(JWT)는 stateless라 Redis 비의존이므로 무관.

---

### Task 스펙 결정 근거

NCP 운영 서버에서 `kubectl top pods -n prod`로 실제 리소스 사용량을 측정한 결과를 기반으로 스펙을 결정했다.

```
$ kubectl top pods -n prod

NAME                          CPU(cores)   MEMORY(bytes)
cm-acme-http-solver-xcbn6     0m           4Mi
redis-6cf6f789c4-lgxxl        4m           6Mi
spot-backend-fdb644b59-52cbw  2m           631Mi
```

#### 측정 결과 분석

| 항목 | 측정값 | 해석 |
|---|---|---|
| CPU | 2m (0.002 vCPU) | 사실상 idle. 외부 API 호출이 많지만 Java 21 Virtual Thread로 I/O 대기 처리라 CPU 소모 낮음 |
| Memory | 631Mi | 평상시(idle) 기준값. `kubectl top`은 순간 스냅샷이라 트래픽 유입 시 피크는 더 높음 |

#### 스펙 결정

**CPU — 0.5 vCPU (Prod 기준)**

평상시 2m 수준이므로 0.5 vCPU(500m)도 충분하다. 이 앱의 병목은 CPU가 아니라 메모리와 외부 API I/O다.

```
0.5 vCPU → 1 vCPU 업그레이드 시
  JVM cold start: 60초 → 30초 (Task 교체 시에만 영향)
  운영 중 응답 속도: 차이 거의 없음 (I/O-heavy 워크로드)
  비용: 2배 증가

→ vCPU 수직 확장 불필요. 부족 시 Task 수평 확장으로 대응.
```

**Memory — 1GB (Prod/Staging 공통 최소사양)**

```
평상시 사용량 (측정값)  631Mi
트래픽 스파이크 예상치  800~900Mi  (GC 주기, 동시 요청 힙 증가)
────────────────────────────────────
1GB 컨테이너 여유       393Mi  ← 빡빡하지만 초기 운영에는 허용 가능
```

`kubectl top`은 GC 직후 idle 시점에 낮게 찍히는 특성이 있어 631Mi가 최솟값에 가깝다.
만약 EC2 였다면 Swap을 사용해 버틸 수 있겠지만, **ECS Fargate는 플랫폼 특성상 Swap 메모리(`linuxParameters`) 설정 자체를 지원하지 않는다.**
따라서 일단 1GB로 시작하되, CloudWatch에서 메모리 사용률이 상시 80% 이상이거나 OOM Kill이 발생하면 즉시 **2GB로 수직 확장**하는 빠른 대응(Fail-fast) 전략을 취한다. (1GB → 2GB 전환 시 비용 차이는 월별 약 $3.2)

> Staging을 0.5GB로 설정하면 동일한 Spring Boot JAR 기동 시 JVM 자체가 OOM으로 죽는다.
> JVM 힙 + Metaspace + 오버헤드 합계가 이미 500MB를 초과하므로 최소 1GB 필요.

**CPU 아키텍처 — ARM64 (Graviton)**

로컬 개발 환경(Mac M1/M2/M3)과의 호환성 및 빌드 속도를 위해, 그리고 동일 vCPU/Memory 대비 약 20% 저렴한 비용과 향상된 성능을 위해 ECS Task Definition의 OS/Architecture를 반드시 `Linux/ARM64`로 설정한다.

---

### Auto Scaling 전략

#### EC2 Auto Scaling vs ECS Fargate Auto Scaling

```
EC2 Auto Scaling
트래픽 급증 → EC2 인스턴스 생성 (AMI 부팅 2~5분) → 컨테이너 시작
너무 느려서 급격한 스파이크 대응 불가

ECS Fargate Auto Scaling
트래픽 급증 → Task(컨테이너)만 추가 (30~60초)
기존 인프라 위에 컨테이너만 늘리므로 빠른 대응 가능
```

#### 앱인토스 노출 대응 시나리오

앱인토스(유명 앱 소개 플랫폼)에 서비스가 노출되면 트래픽이 단기간에 급증할 수 있음.

```
[반응형] Auto Scaling 설정
CloudWatch 알람: ALB RequestCount > 1000 req/Task
  → ECS Task 자동 추가 (최대 5개)
  → 트래픽 안정화 후 자동 축소

[예방형] 노출 전날 수동 스케일 업
aws ecs update-service \
  --cluster moisam-cluster \
  --service moisam-prod \
  --desired-count 3

노출 종료 후 다시 축소
aws ecs update-service \
  --cluster moisam-cluster \
  --service moisam-prod \
  --desired-count 1
```

#### Task 수에 따른 예상 트래픽 처리량

```
Task 1개 (0.5vCPU / 1GB)  →  동시 사용자 약 50~100명
Task 3개                   →  동시 사용자 약 150~300명
Task 5개                   →  동시 사용자 약 250~500명
```

#### RDS 연결 수 주의 (스케일 아웃 시 병목)

ECS Task가 늘어나면 DB 연결 수도 함께 증가함.

```
db.t4g.micro 최대 커넥션: 약 87개
HikariCP 기본 풀 사이즈:  10개/Task

Task 5개 × 10 = 50개  ✅
Task 9개 × 10 = 90개  ❌ RDS 한계 초과
```

HikariCP 풀 사이즈를 줄여 Task를 더 많이 띄울 수 있도록 조정.

```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5   # 기본 10 → 5로 축소
      minimum-idle: 2
# Task 15개 × 5 = 75개로 안전하게 운영 가능
```

---

## CI/CD 파이프라인

```
[GitHub]
  │
  ├─ PR → develop    : Run Tests (Testcontainers)
  │
  ├─ Merge → develop : Build → ECR Push → EC2 SSH 배포 (Docker)
  │
  └─ Merge → main    : Build → ECR Push → ECS Prod 배포
```

### ECS CPU/Memory 단위

```
vCPU 0.25 → cpu: "256"     vCPU 0.5 → cpu: "512"
Memory 1GB → memory: "1024"    Memory 2GB → memory: "2048"
```

### Prod 배포 (.github/workflows/prod-deploy.yml)

모듈화된 두 개의 작업을 통해 빌드와 배포를 분리하며, ARM64 아키텍처에 최적화된 이미지 빌드를 수행합니다.

```yaml
jobs:
  build:
    name: "Build and Push"
    # ... Gradle 빌드 및 Docker ARM64 이미지 생성 (QEMU/Buildx 적용)
    # Login to ECR 후 이미지 URI를 Output으로 전달
    
  deploy:
    name: "Deploy to ECS"
    needs: build
    # ... AWS task-definition 다운로드 및 정제 (jq)
    # 넘겨받은 이미지 URI로 서비스 업데이트
```

### 주의사항 및 보안
- **GitHub Secrets**: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `GH_TOKEN` 등 관리
- **이미지 최적화**: 로컬 개발 환경(M1/M2)과 동일한 **ARM64** 기반 이미지를 생성하여 비용 절감 및 호환성 확보
- **Task 정제**: `jq`를 사용하여 `revision`, `status` 등 불필요한 필드를 제거해야 ECS 배포가 안정적으로 수행됨

---

## 향후 개선 사항 (Security & Ops)

### 1. GitHub OIDC 전환 (보안 강화)
- **현재**: 정적 IAM User Access Key 사용
- **개선**: AWS OIDC(OpenID Connect)를 설정하여 GitHub Actions가 단기 토큰을 사용하여 AWS에 인증하도록 전환. 정적 키 유출 위험을 근본적으로 제거.

### 2. SSM Parameter Store 도입 (관리 최적화)
- **현재**: `application-prod.yml` 파일 전체를 GitHub Secrets에 저장
- **개선**: 개별 환경변수(DB_URL, JWT_SECRET 등)를 AWS SSM Parameter Store에 저장하고, ECS Task Definition에서 직접 참조하도록 변경. CI/CD 작업 시 민감 정보 노출을 최소화.

### 3. 이미지 보안 스캔 (Trivy/Inspector)
- **개선**: CI 파이프라인 중 이미지 푸시 전 `Trivy` 스캔 단계를 추가하여 OS 및 라이브러리 취약점 조기 발견.

---

## 서비스별 역할

| 서비스 | 역할 | 비고 |
|---|---|---|
| Gabia | 도메인 보유 | 네임서버를 Route53으로 위임 |
| Route53 | DNS | ALIAS 레코드로 apex 도메인 포함 ALB 직접 연결 |
| ACM | SSL 인증서 | 직접 발급, 자동 갱신, 무료 |
| ALB | 리버스 프록시, 라우팅 | 단일 ALB로 Prod/Staging 처리 |
| ECS Fargate | Prod 서버 | Spring Boot, 상시 실행, Auto Scaling |
| EC2 | Staging 서버 | Docker 스크립트 기반 배포, 테스트 전용 단일 인스턴스 |
| ElastiCache | Redis | Prod/Staging 공유, prefix 구분. Rate Limiter 공유 필수라 컨테이너화 불가 |
| RDS | PostgreSQL | 기존 그대로 유지 |
| ECR | 이미지 저장소 | Prod/Staging 동일 이미지, 태그 구분 |
| S3 | 파일 저장 | 이미지 업로드 (기존 코드 그대로) |
| SSM Parameter Store | 시크릿 관리 | DB URL, JWT Secret 등 |
| CloudWatch Logs | 로그 | ECS 자동 수집 |
| Discord Webhook | 에러 알림 | 기존 코드 활용 |

---

## 월 비용

```
ECS Fargate Prod (0.5vCPU / 1GB)             $18.03
EC2 Staging (t4g.micro 등)                    $6.00 (비상용 혹은 Spot)
ALB                                           $16.00
ElastiCache cache.t4g.micro                   $12.00
RDS db.t4g.micro                              $12.41
ECR                                            $1.00
S3                                             $2.00
SSM Parameter Store                            $0.00  무료
CloudWatch Logs                                $0.00  무료 티어
Gabia                                          $0.00  도메인 기보유
ACM                                            $0.00  무료
───────────────────────────────────────────────────────
합계                                          $65.63 / 월

$1,000 ÷ $65.63 = 약 15.2개월 운영 가능
OOM 발생 시 Prod 2GB 전환  → $68.79 / 월 (약 14.5개월)
```

### Prod Task 메모리 스케일업 전략

1GB + Swap 512MB로 시작하고, OOM 징후 발생 시 메모리를 증가시키는 방향으로 운영.

**Fargate 0.5 vCPU 지원 메모리 옵션**

```
1GB  → 2GB  → 3GB  → 4GB   (1GB 단위 증가만 지원)

※ 1.5GB는 Fargate에서 지원하지 않음
   0.5 vCPU 기준 1GB 단위로만 설정 가능
```

**OOM 발생 시 대응 방법**

```bash
# 1. CI/CD 워크플로우에서 memory 값 변경 후 배포
echo $(jq '.memory = "2048"' task-definition.json) > task-definition.json

# 2. 또는 AWS 콘솔에서 직접 변경 (5분 이내)
#    ECS → Task Definitions → moisam-prod → 새 리비전 생성 → Memory 2048
```

---

## 코드 수정 사항

### application-prod.yml / application-stg.yml

```yaml
server:
  forward-headers-strategy: framework  # ALB 뒤에서 X-Forwarded-* 처리
```

### application-stg.yml

```yaml
spring:
  data:
    redis:
      key-prefix: "staging:"  # ElastiCache 공유 시 Prod 키와 충돌 방지
```

---

## 마이그레이션 순서 (점진적 이전 전략)

현재 운영 중인 EC2와 RDS에 영향을 주지 않고 새 VPC에 ECS 환경을 먼저 구축한 뒤, 마지막에 트래픽과 데이터를 전환하는 안전한 방식(Zero-Downtime 지향)으로 전환하여 진행합니다.

```text
✓ 1단계 — 기본 네트워크 구성 (완료)
  └─ [x] 신규 VPC 생성 (10.0.0.0/16)
  └─ [x] Public Subnet (2개), Private Subnet (2개) 생성
  └─ [x] Internet Gateway 생성 및 VPC 연결
  └─ [x] Route Table 생성 및 서브넷 연결 (Public/Private 분리)

2단계 — ECS 인프라 및 기반 서비스 구축 (새 VPC에 운영 환경 복제)
  └─ [x] ECR 레포 생성
  └─ [x] ECS용 Security Group 생성
  └─ [x] 환경변수 등록 (`application-prod.yml` 파일 그대로 활용, `SPRING_PROFILES_ACTIVE=prod` 적용)
  └─ [x] ACM 인증서 발급 (DNS 검증 → Route53 연동)
  └─ [x] ALB 생성 (Public Subnet) 및 Target Group 연결
  └─ [x] ECR에 이미지 Push (ARM64 아키텍처)
  └─ [x] ECS 클러스터 및 배포 (Prod Service 생성, 단 DB 전환 전까지 Desired 0 유지)

3단계 — 데이터 마이그레이션 (DB/Cache 이전)
  └─ [x] DB용 Subnet Group 생성 (Private Subnet)
  └─ [x] DB/Cache용 Security Group 생성
  └─ [x] 기존 RDS 스냅샷 생성 → 새 VPC Private Subnet으로 복원
  └─ [x] ElastiCache 생성 (Private Subnet)
  └─ [ ] ECS의 환경변수(DB 호스트 등)를 신규 리소스로 변경 및 재배포

4단계 — 트래픽 전환 (무중단 오픈)
  └─ [ ] Route53 호스팅 존 생성 및 ALB ALIAS 레코드 세팅
  └─ [ ] Gabia 네임서버 → Route53 NS 레코드로 변경 (트래픽 서서히 전환됨)
  └─ [ ] (선택) 구 버전 EC2/RDS 모니터링 후 안전하게 제거

5단계 — CI/CD 연결
  └─ [ ] GitHub OIDC → AWS IAM Role 설정
  └─ [ ] GitHub Actions Workflow 배포 테스트
```
