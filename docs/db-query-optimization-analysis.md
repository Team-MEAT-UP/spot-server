# JPA 쿼리 성능 최적화 및 실행계획(EXPLAIN) 분석 리포트

## 개요
이 문서는 코드랩 시스템 내에서 애플리케이션의 응답 속도를 저하시킬 수 있는 주요 JPA 쿼리들을 모색하고, 실제 PostgreSQL 데이터베이스 엔진의 실행계획(Execution Plan) 분석을 바탕으로 최적화를 진행한 사례를 정리하였습니다.

---

## 1. 공간 데이터 탐색 최적화 (PostGIS KNN 활용)

### ❌ AS-IS (최적화 전)
- **대상:** `SubwayRepository.findClosestSubway`
- **구조:** `WHERE ST_Distance(point) = (SELECT MIN(ST_Distance(point)))`
- **실행 계획 구조:**
```text
Seq Scan on subway s  (cost=0.00..452.31 rows=1 width=32)
  Filter: (st_distance(point, '[주어진 좌표]') = $0)
  InitPlan 1 (returns $0)
    -> Aggregate  (cost=226.15..226.16 rows=1 width=8)
       -> Seq Scan on subway s2  (cost=0.00..184.20 rows=839 width=32)
```
- **문제 원인:** 사용자와 가장 가까운 지하철역 하나를 찾기 위해 `MIN()` 서브쿼리 내부에서 1차적으로 전체 테이블 스캔(Seq Scan)을 통해 거리를 계산합니다. 이후, 계산된 최소값과 일치하는 역을 찾기 위해 다시 한번 Seq Scan을 수행하므로 O(N)의 복잡도와 엄청난 공간 연산 오버헤드를 발생시킵니다.

### ✅ TO-BE (최적화 후)
- **수정:** `ORDER BY point <-> '[주어진 좌표]' LIMIT 1`
- **실행 계획 구조:**
```text
Limit  (cost=0.15..0.21 rows=1 width=32)
  -> Index Scan using subway_point_gist_idx on subway s  (cost=0.15..52.30 rows=839)
       Order By: (point <-> '[주어진 좌표]')
```
- **개선 결과:** PostGIS 전용 K-Nearest Neighbor(KNN) 연산자인 `<->` 연산자와 `LIMIT 1`을 사용하여 테이블 풀 스캔을 완전히 제거하였습니다. 데이터베이스 옵티마이저가 공간 인덱스(GiST) 트리를 타고 내려가 가장 인접한 노드 1개를 찾는 즉시 탐색을 종료(Index Scan)하게 되어 속도가 극적으로 향상됩니다 (O(log N)).

---

## 2. 어드민 페이징 쿼리의 풀 테이블 스캔 제거

### ❌ AS-IS (최적화 전)
- **대상:** `EventRepository.findFilteredEvents`
- **구조:** `LEFT JOIN (SELECT event_id, COUNT(*) FROM start_point GROUP BY event_id)` 형태로 조건 없이 `start_point` 테이블 전체 스캔
- **실행 계획 구조:**
```text
Hash Right Join  (cost=412.30..819.12 rows=...)
  Hash Cond: (pc.event_id = e.event_id)
  -> HashAggregate  (cost=350.00..370.00 rows=2000)
       Group Key: sp.event_id
       -> Seq Scan on start_point sp  (cost=0.00..250.00 rows=20000)
  -> Bitmap Heap Scan on event e ...
```
- **문제 원인:** 검색된(필터링된) 이벤트의 참석자 수를 세기 위해, 옵티마이저가 무조건 `start_point` 테이블 전체 레코드를 읽어 들이고(Seq Scan) `event_id` 기준으로 해시 집계를 처리(Hash Aggregate)해둔 뒤 조인을 수행합니다. 이는 데이터 규모(N)가 커질수록 서버 장애나 치명적인 지연을 유발합니다.

### ✅ TO-BE (최적화 후)
- **수정:** WHERE 조건 안벌에 `(SELECT COUNT(*) FROM start_point WHERE event_id = e.event_id) = :count`와 같이 상관 서브쿼리(Correlated Subquery)로 변경
- **실행 계획 구조:**
```text
Nested Loop  (cost=...)
  -> Index Scan on event e  (cost=... Filter: created_at)
  -> Aggregate
       -> Index Only Scan using idx_start_point_event_id on start_point sp
            Index Cond: (event_id = e.event_id)
```
- **개선 결과:** 기간 등에 의해 1차 필터링된 건수의 `event`들에 한해서만 Nested Loop 방식으로 서브쿼리가 동작합니다. 이때 옵티마이저가 인덱스를 직접 찌르며 탐색(Index Only Scan)을 수행하므로, 불필요한 테이블 풀스캔과 전체 그룹 다루기가 완전히 사라졌습니다.

---

## 3. 유저 데이터 지연 로딩(Lazy Loading) 및 N+1 문제 해결

### ❌ AS-IS (최적화 전)
- **대상:** `ReviewRepository.findAllByPlace`
- **현상:** 리뷰(Review) 목록과 화면에 노출될 유저(User)의 닉네임을 가져올 때 `Long User = review.getUser()` 방식으로 호출 시, 리뷰 개수만큼의 단건 SQL 호출이 발생
- **문제 원인:** LAZY 관계로 엮인 `User` 객체의 프로퍼티(nickname, profileImage)를 파싱하는 과정에서 **N+1 쿼리 이슈** 발생. (1번의 리뷰 조회를 위해 N번의 부가적인 Select 쿼리가 전송되어 Connection과 Network RTT 낭비 심각)

### ✅ TO-BE (최적화 후)
- **수정:** `@EntityGraph(attributePaths = {"user"})` 어노테이션 추가
- **개선 결과:**
```sql
-- 데이터베이스 덤프 로그 확인
SELECT r.*, u.* FROM review r 
LEFT OUTER JOIN user u ON r.user_id = u.id 
WHERE r.place_id = ?
```
별도의 부가 쿼리들 없이, 단일 데이터베이스 접근(`LEFT OUTER JOIN`) 한 번으로 필요한 유저 정보까지 영속성 컨텍스트에 메모리 로드(Fetch Join) 하여 불필요한 네트워크 I/O 병목을 해결하였습니다.
