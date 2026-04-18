# Work Experience

---

## [아도스](https://adoscompany.com/) - 솔루션사업부 (2025.11 ~ )

### **보안 솔루션 운영 및 기술지원 업무**

| 제목 | 요약 |
| --- | --- |
| 기술지원(VoC) 대응 및 문서화 |   • **월 평균 60건** 이상의 기술 문의 해결\n  • 대응 내용을 문서화해 팀 전체 VoC 해결률 향상에 기여 |
| 제품 고도화 기여 | **5개월간 약 100건**의 이슈 등록 (팀 평균 대비 2배 수준) |
| 폐쇄망 테스트 인프라 구축 |   • 사내 폐쇄망 내 유휴 서버를 활용하여 **운영 환경과 동일한 모듈 검증 환경** 구축\n  • 폐쇄망 모듈을 패치 전에 테스트하여 안정적인 패치 수행 |
| QA 자동화 도입 |   • Cypress 기반 웹 QA 자동화 체계 수립\n  • 전체 테스트 케이스의 **50% 자동화 달성 (328/660건)**으로 반복 업무 리소스 절감 |
| 패치 프로세스 안정화 |   • 패치 중 **사용자 요청 차단 로직(Inbound IP Rule)** **도입**\n  • 구버전 모듈과의 충돌 및 런타임 오류를 차단하여 안정성 향상 |
| 고객사 서버 구축 | IIS 설치부터 모듈 적용까지 고객사 현장에 직접 방문해 서버 환경 구축 진행 |

### 장애 대응 수행

| 제목 | 요약 |
| --- | --- |
| 파일 서버 업로드 불가 이슈 해결 |   **• 장애 분석\n      ◦** 파일 서버 비밀번호 변경 후 발생한 간헐적 업로드/다운로드 장애를 분석\n      ◦ 기존 SMB(포트 445) TCP 세션과 신규 인증 세션 간 공존 상태에서 문제 발생\n  • **트러블슈팅:** `netstat`을 통해 접속 경로(호스트명 vs IP 주소)에 따라 자격 증명이 다르게 생성됨을 파악\n  • **조치:** 접속 경로(Path) 교체 방식을 적용하여 기존 세션의 간섭 없이 신규 자격 증명 세션을 생성하게 함으로써 다운타임 없이 서비스 운영 |
| 고객사 PC 느려짐 이슈 해결 |   **• 장애 분석\n      ◦ 자**사 프로그램 사용 중 PC가 느려지는 현상 발생\n      ◦ 이벤트뷰어에서 로그 확인 결과, 보안 프로그램의 필터와 자사 프로그램의 필터가 간섭하는 시간대에 PC 느려짐 확인\n  **• 조치**: 보안 프로그램을 하나씩 제거하며 테스트해 원인 프로그램 특정, 해당 프로그램 재거 후 증상 해소 |

# Projects

---

## 1. [모이삼](https://www.moisam.kr/)- 합리적인 중간지점 추천 서비스 (25.04 ~)

**Github** | https://github.com/Team-MOISAM/moisam-server

```
- Java 21 + Spring Boot 3.4.4
- PostgreSQL(PostGIS), JPA, Redis
- AWS ECS, EC2, RDS, Elasticache, ALB, ECR, S3
- Github Actions
```

### **중간지점 추천 알고리즘 설계 및 최적화**

| 제목 | 요약 |
| --- | --- |
| [수도권 지하철역 데이터 파이프라인 구축](https://www.notion.so/26df223eab5c80f8b098ff9ed8c9a091?pvs=21) |   • 수도권 지하철역·환승 정보 및 소요 시간 데이터 모델링 |
| 중간지점 후보군 탐색 알고리즘 개발 |   • Floyd-Warshall 알고리즘으로 계산된 경로 데이터를 캐싱하여 실시간 조회 성능 향상\n  • 요청마다 경로를 탐색하던 기존 방식에서 **O(1) 조회**로 전환하여 API 응답 시간 단축 |

### 외부 API 안정성 확보

| 제목 | 요약 |
| --- | --- |
| [Redis 캐싱을 통한 성능 최적화](https://www.notion.so/Redis-26df223eab5c803a9b1ad2ec1f4058cd?pvs=21) |   • Cache Aside / Write Around 패턴을 적용으로 불필요한 외부 API 호출 최소화 및 경로 데이터 조회 속도 개선  |
| [Rate Limiter 구현 및 모니터링 시스템 구축](https://www.notion.so/Rate-Limiter-26df223eab5c809f8a24d5faa5e6068c?pvs=21) |   • 멀티스레드 환경에서 원자성 보장을 위해 Lua Script 기반 Rate Limit 적용\n  • 일일 호출 한도 임박/초과 이벤트 발생 시 Discord Webhook을 통한 실시간 모니터링 구축 |
| Resilience4j를 통한 회복탄력성 확보 |   • Odsay, Kakao API 등 외부 API 장애가 내부 시스템으로 전파되는 것을 차단\n  • **Retry(3회), Circuit Breaker, Timeout(1.5-2s)** 설정을 통해 가용성 유지 |
| **Gzip 커스텀 필터링을 통한 응답 속도 개선** |   • 대용량 경로 데이터 전송 개선을 위해 Gzip 압축 (CompressFilter) 적용\n  • JSON 페이로드 크기를 **약 70~80% 감소하여 응답 속도 개선** |

### 트러블슈팅

| 제목 | 요약 |
| --- | --- |
| **DB 커넥션 고갈 장애 해결** |   • **원인:** EC2 중개 서버에서 ‘socat(fork 옵션)’을 사용해 포트 포워딩 시, 요청마다 생성된 자식 프로세스가 회수되지 않고 누적되면서 **RDS Max Connections 초과**\n  • **해결**:  PostgreSQL 전용 커넥션 풀러인 ‘**PgBouncer’**를 도입하고, 클라이언트 세션 오류를 막기 위해 ‘pool_mode’를 ‘session’으로 변경하여 동일 장애 발생 발생률 0% 달성 |
| [Layered Jar 적용을 통한 도커 배포 시간 단축](https://developer-anxi.tistory.com/93) |   • 소스 코드 수정 시 100MB 이상의 Fat Jar 전체가 매번 재생성 및 배포되는 문제를 해결하기 위해 **Layered Jar** 도입\n  • **이미지 Pull 시간 75%**(4s → 1s) 및 **초기 가동 시간 52%**(44s → 21s) 단축 달성 |

### **비즈니스 기여 및 협업**

| 제목 | 요약 |
| --- | --- |
| [어드민(Admin) 대시보드 구축 및 MAU 300% 성장](https://developer-anxi.tistory.com/91) |   • 팀원들과 Google Analytics(GA) 데이터를 분석하여 이탈률이 높은 채널 대신 블로그 및 에브리타임 등 고효율 타겟 채널로 홍보 전략 주도\n  • GA 데이터 교차 검증 및 마케팅 성과 확인을 위해 **어드민(Admin) 페이지를 구축\n  •** IT 커뮤니티 투고를 통해 '디스콰이엇(Disquiet) 이 주의 프로덕트' 2회 연속 선정 및 한 달 만에 MAU 300% 성장을 이뤄냈으며, 이후 지속적인 홍보를 통해 최대 활성 사용자 1,300명 달성 |
| 팀 개발 문화 주도 |   • [모이삼 자체 Git-Flow](https://developer-anxi.tistory.com/82) 및 Commit 룰 정립\n  • PR 리뷰 활성화\n      ◦ 리뷰이 규칙 (D-n) + 리뷰어 규칙 (Pn) |
| [기획 파트와 개발 파트 수평적인 소통 담당](https://www.notion.so/26df223eab5c8051ba5ac3740121aad4?pvs=21) |   • 디스코드 스레드를 적극 활용하여 개발 파트의 적극적인 소통\n  • ‘고민거리’ 채널을 만들어 팀 전체가 자유롭게 고민 공유 및 자유로운 대화 유도 |

---

## 2. 조각조각 - 개인 맞춤형 활동 추천 서비스 (24.10 ~ 24.11)

**Github** | https://github.com/KUSITMS-30th-TEAM-C/backend

```
- Java 17 + Spring Boot 3.3.4
- MySQL, JPA, RabbitMQ
- NCP Server, Container Registry, AWS RDS
- Github, Docker
```

### 주요 활동

| 제목 | 요약 |
| --- | --- |
| 도메인 주도 설계(DDD) 도입 | - 도메인 중심으로 아키텍처를 설계하여 비즈니스 로직 분리 \n- 의존성 역전 원칙(DIP)을 적용해 계층 간 결합도 최소화 |
| [활동 자동 종료 기능 개발](https://developer-anxi.tistory.com/68) | - 사용자가 입력한 잔여 시간이 지나면 활동 상태를 자동으로 종료하는 기능 구현 \n- RabbitMQ Delayed Message Plugin을 사용해 비동기 이벤트 기반 구조로 설계 |

---

## 3. 기타 경험

### 외부 활동

| 연도 | 활동 |
| --- | --- |
| 2025 | - KUSITMS(한국대학생IT경영학회) 31기 교육기획팀 운영진\n- 코드리뷰 및 발표 스터디 운영\n- 네이버 클라우드 `CERTIFIED Nclouder` 선정 ([2025.09](https://blog.naver.com/n_cloudplatform/224017681294)) |
| 2024 | - KUSITMS 30기 백엔드 파트원 |
| 2023 | - 단국대학교 멋쟁이사자처럼 11기 프론트엔드 파트원 |

---

## 📝 향후 계획 (TODO)

### 🚀 GCP 환경 기반 정밀 부하 테스트 및 성능 개선
기존의 러프한 측정 방식을 넘어, 실제 트래픽이 몰리는 상황을 가정하여 정밀한 Load Testing을 수행하고 시스템의 물리적 한계를 파악합니다.

**1. 부하 테스트 인프라 구성**
- GCP 서버에 독립적인 부하 테스트 워커(nGrinder, k6, JMeter 등) 구축
- 대상 서버(AWS ECS)와 물리적으로 분리하여 네트워크 대역폭 제한 없는 클라이언트 부하 발생 환경 보장

**2. 주요 측정 대상 (Metrics)**
- **응답 시간(Latency, p95/p99)**: '중간지점 탐색' 등 주요 하드 연산 로직의 지연 시간 방어율 측정
- **임계점(Point of Failure)**: TPS/RPS 성장 곡선이 꺾이거나 에러율이 급증하는 동시 사용자(VUser) 수 파악
- **리소스 사용률 추적**: ECS 컨테이너 자원(CPU/Memory), RDS(Active Session, Connection), Redis 캐시 히트율 및 메모리 추이 모니터링

**3. 핵심 테스트 시나리오**
- **탐색 알고리즘 캐싱 성능 점검**: Floyd-Warshall O(1) 캐싱 로직 및 Redis가 대용량 트래픽에서 병목 없이 제대로 동작하는지 증명
- **PgBouncer 커넥션 풀링 성능 테스트**: 수많은 동시 요청 발생 시 `pool_mode` 튜닝이 RDS를 어떻게 온전히 복구/보호하는지 검증
- **Rate Limit & Circuit Breaker 내결함성 테스트**: 다량의 외부 API 요청 발생 시, 설정해둔 Timeout/Fallback 로직이 서버 연쇄 장애를 성공적으로 막아내는지 테스트
