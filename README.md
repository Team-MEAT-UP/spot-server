# 📍 모두를 위한 하나의 SPOT

> 🔗 **배포 주소** : https://www.pickspot.co.kr

<img width="1247" alt="spot" src="https://github.com/user-attachments/assets/6948789d-d053-41c5-99f1-ac7fa12d82a4" />

# 💻 Development

## 🗒️ API 명세서

> 🔗 [API 명세서](https://valley-tenor-1ca.notion.site/API-1c7f3199866980d388f4f9da3aa78fb0)

> 🔗 [Swagger](https://api.pickspot.co.kr/swagger-ui/index.html)

## 🏛️ System Architecture

![system](https://github.com/user-attachments/assets/c5dcde53-06eb-49df-9134-00cbad2e144a)

## 📊 ERD

![erd](https://github.com/user-attachments/assets/433d9a1d-4738-478a-bfb3-c0f61ba6ca1c)

# 🚀 Backend

## Backend 기술 스택

| 기술 스택                   | 주요 활용 및 선택 이유                                                                           |
| --------------------------- | ------------------------------------------------------------------------------------------------ |
| **Java 21**                 | 장기 지원(LTS, ~2031) 제공, Virtual Thread로 다중 외부 API 호출 최적화                           |
| **Spring Boot 3.4.4**       | Java 21 공식 지원, Virtual Thread 최적화, RestClient 및 Actuator 등 최신 기능 포함               |
| **Spring Data JPA**         | JPA 기반 ORM 구현, Repository로 CRUD 자동화, QueryDSL과의 연계로 복잡한 쿼리 간결 처리           |
| **PostgreSQL + PostGIS**    | 인덱스 최적화로 저장 효율 증가, PostGIS로 대용량 공간 좌표 데이터 처리 가능                          |
| **Redis**                   | 인메모리 캐시로 실시간 경로 조회 성능 향상, TTL 기반 캐싱으로 멱등성 보장                        |
| **Docker & Docker Compose** | 컨테이너 단위 배포로 확장성 높음, 컴포넌트별 독립 실행 및 관리 용이                              |
| **GitHub Actions**          | 코드 푸시 시 자동 CI/CD 실행, GUI 기반 모니터링으로 개발 편의성 향상                             |
| **QueryDSL**                | 타입 안전한 DSL 기반 쿼리 작성, 컴파일 타임 오류 검출로 안정성 확보                              |
| **Naver Cloud Platform**    | 유연한 배포/네트워크 구성, 한글 문서로 접근성 우수, 컨테이너/레지스트리/Object Storage 연동 용이 |

## Directory 구조

```
├── ServerApplication.java       
├── 📁auth                         // 인증, 인가 도메인
│   ├── application                
│   ├── dto                        
│   ├── exception                  
│   ├── presentation               
│   └── support                   
├── 📁batch                        // 배치 처리
│   ├── place                      
│   └── presentation              
├── 📁event                        // 이벤트 도메인
│   ├── application
│   ├── domain
│   ├── dto
│   ├── exception
│   ├── implement
│   ├── persistence
│   └── presentation
├── 📁global                       // 전역 공통 기능
│   ├── clients                    
│   ├── config                     
│   ├── domain                     
│   ├── presentation               
│   ├── support                    
│   └── util                       
├── 📁parkinglot                   // 주차장 도메인
│   ├── domain
│   ├── implement
│   ├── infrastructure
│   └── persistence
├── 📁place                        // 장소 도메인
│   ├── application
│   ├── domain
│   ├── dto
│   ├── exception
│   ├── implement
│   ├── persistence
│   └── presentation
├── 📁review                       // 리뷰 도메인
│   ├── application
│   ├── domain
│   ├── dto
│   ├── exception
│   ├── implement
│   ├── persistence
│   └── presentation
├── 📁startpoint                   // 출발지 도메인
│   ├── application
│   ├── domain
│   ├── dto
│   ├── exception
│   ├── implement
│   ├── persistence
│   ├── presentation
│   └── util
├── 📁subway                       // 지하철 도메인
│   ├── domain
│   ├── dto
│   ├── exception
│   ├── implement
│   ├── infrastructure
│   └── persistence
└── 📁user                         // 사용자 도메인
    ├── application
    ├── domain
    ├── dto
    ├── exception
    ├── implement
    ├── persistence
    └── presentation
```

**📌 Presentation Layer (`presentation`)**

- **역할:** 외부 요청을 받아들이는 API 엔트리 포인트
- **포함 요소:** `@RestController`, `@RequestMapping`, `@RequestBody` 등의 어노테이션을 사용한 클래스
- **특징:** 외부 변화에 민감하며, 요청/응답 변환, 예외 처리 등을 담당

---

**📌 Business Layer (`application`)**

- **역할:** 유스케이스 기반의 도메인 비즈니스 흐름을 처리
- **포함 요소:** `Service` 클래스들, 비즈니스 트랜잭션 관리
- **특징:** Implement Layer 간 협력, 흐름 제어 등을 담당

---

**📌 Implement Layer (`implement`)**

- **역할:** 실제 기능의 구체적인 구현체
- **포함 요소:** 비즈니스 레이어에서 사용하는 로직 구현체
- **특징:** 재사용성이 높고, 상세한 도구 로직 존재

---

**📌 Data Access Layer (`persistence`)**

- **역할:** 데이터베이스와 직접 연결되어 데이터를 저장, 조회, 수정, 삭제하는 기능을 담당
- **포함 요소:** `JpaRepository`, `JPAQueryFactory` 등 데이터 접근을 위한 구현체 및 인터페이스
- **특징:** DB와의 통신 및 쿼리 실행에 특화

## Commit Convention

| Type | 내용 |
| --- | --- |
| `feat` | 새로운 기능 구현 |
| `chore` | 부수적인 코드 수정 및 기타 변경사항 |
| `docs` | 문서 추가 및 수정, 삭제 |
| `fix` | 버그 수정 |
| `test` | 테스트 코드 추가 및 수정, 삭제 |
| `refactor` | 코드 리팩토링 |
| `ci/cd` | CICD 설정 |

## 성능 최적화

> **무한 스크롤 쿼리 최적화**

- slice 방식의 무한 스크롤 방식과 쿼리 작성 시 fetch join 을 통해서 쿼리 발생을 감소 시켰습니다
- 이 과정에서 QueryDSL 사용을 통해 복잡한 조건의 동적 쿼리를 쉽게 구성하였습니다

> **Redis 기반 실시간 캐싱 처리**

- 외부 API를 활용한 실시간 경로 조회 및 중간 지점 계산 시, 디스크 기반 DB 접근(I/O)을 줄이기 위해 **인메모리 NoSQL인
  Redis**를 도입하였습니다.
- 외부 API 응답을 Redis에 캐싱하고, **TTL(Time To Live)** 을 적용하여 **멱등성 보장 및 중복 호출 방지**를
  실현하였습니다. 이를 통해 **외부 API 호출 횟수 제한 정책에 유연하게 대응**할 수 있도록 구성하였습니다.

> **주소 파싱 시 불필요한 Pattern 객체 생성 제거**

- 기존에는 `address.split(" ")`을 사용하여 첫 번째 지역명을 추출했지만, 이는 내부적으로 `Pattern.compile()`을 통해
  정규표현식 객체를 생성하게 됩니다.
- 이를 `substring()`과 `indexOf()`기반으로 변경함으로써 **Pattern 객체 생성을 방지**하고, **GC 부담을 줄여 문자열 처리
  성능을 확보**하였습니다.
  
![performance](https://github.com/user-attachments/assets/79001665-0321-48df-89b2-f3d4c34e16cc)
