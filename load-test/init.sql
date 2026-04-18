-- =====================================================
-- 부하 테스트용 시드 데이터 (init.sql)
-- docker-compose 실행 시 PostgreSQL 초기화 단계에서 자동 실행됨
--
-- ⚠️  테이블 생성은 Spring Boot ddl-auto: create가 담당
--    → 앱 기동 완료 후 이 파일은 이미 실행된 상태가 아닐 수 있음
--    → 앱 기동 후 아래 INSERT를 수동으로 실행하거나,
--      02-seed.sql을 별도로 실행할 것
-- =====================================================

-- ── 테스트 유저 (기준 데이터) ──
INSERT INTO users (id, email, nickname, profile_image, role, created_at, updated_at)
VALUES
  (1, 'loadtest1@moisam.kr', '부하테스터1', 'https://example.com/img1.png', 'USER', NOW(), NOW()),
  (2, 'loadtest2@moisam.kr', '부하테스터2', 'https://example.com/img2.png', 'USER', NOW(), NOW()),
  (3, 'loadtest3@moisam.kr', '부하테스터3', 'https://example.com/img3.png', 'USER', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ── 테스트 이벤트 (k6 common.js의 TEST_DATA.eventIds와 동기화) ──
INSERT INTO events (id, title, date, created_at, updated_at)
VALUES
  ('aaaaaaaa-0000-0000-0000-000000000001', '부하테스트 이벤트 1', '2026-05-01', NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000002', '부하테스트 이벤트 2', '2026-05-02', NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000003', '부하테스트 이벤트 3', '2026-05-03', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ── 출발지 (각 이벤트에 3명 참여자 → 중간지점 계산 가능) ──
-- 서울 주요 지점 좌표 사용 (WGS84)

-- 이벤트 1 출발지: 강남, 홍대, 잠실
INSERT INTO start_points (id, event_id, user_id, name, latitude, longitude, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000001', 1, '강남역', 37.4979, 127.0276, NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000001', 2, '홍대입구역', 37.5572, 126.9221, NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000001', 3, '잠실역', 37.5133, 127.1002, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- 이벤트 2 출발지: 신촌, 건대입구, 이태원
INSERT INTO start_points (id, event_id, user_id, name, latitude, longitude, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000002', 1, '신촌역', 37.5554, 126.9366, NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000002', 2, '건대입구역', 37.5403, 127.0694, NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000002', 3, '이태원역', 37.5346, 126.9942, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- 이벤트 3 출발지: 사당, 서울대입구, 수원
INSERT INTO start_points (id, event_id, user_id, name, latitude, longitude, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000003', 1, '사당역', 37.4766, 126.9816, NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000003', 2, '서울대입구역', 37.4811, 126.9527, NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000003', 3, '수원역', 37.2663, 127.0028, NOW(), NOW())
ON CONFLICT DO NOTHING;
