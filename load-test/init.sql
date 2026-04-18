-- =====================================================
-- 부하 테스트용 시드 데이터 (init.sql) - JPA 최신화 버전
-- =====================================================

-- ── 테스트 유저 ──
INSERT INTO users (user_id, email, nickname, profile_image, social_id, personal_info_agreement, marketing_agreement, role, created_at, modified_at)
VALUES
  (1, 'loadtest1@moisam.kr', '부하테스터1', 'https://example.com/img1.png', 'social_1', true, true, 'USER', NOW(), NOW()),
  (2, 'loadtest2@moisam.kr', '부하테스터2', 'https://example.com/img2.png', 'social_2', true, true, 'USER', NOW(), NOW()),
  (3, 'loadtest3@moisam.kr', '부하테스터3', 'https://example.com/img3.png', 'social_3', true, true, 'USER', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- ── 테스트 이벤트 (k6 common.js의 TEST_DATA.eventIds와 동기화) ──
INSERT INTO event (event_id, event_name, event_date_time, created_at, modified_at)
VALUES
  ('aaaaaaaa-0000-0000-0000-000000000001', '부하테스트 이벤트 1', '2026-05-01 12:00:00', NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000002', '부하테스트 이벤트 2', '2026-05-02 12:00:00', NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000003', '부하테스트 이벤트 3', '2026-05-03 12:00:00', NOW(), NOW())
ON CONFLICT (event_id) DO NOTHING;

-- ── 출발지 (각 이벤트에 3명 참여자) ──
-- 이벤트 1 출발지: 강남, 홍대, 잠실
INSERT INTO start_point (start_point_id, event_id, user_id, start_point_name, is_user, is_transit, road_longitude, road_latitude, address, road_address, point, created_at, modified_at)
VALUES
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000001', 1, '강남역', true, true, 127.0276, 37.4979, '서울 강남구', '강남대로', ST_SetSRID(ST_MakePoint(127.0276, 37.4979), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000001', 2, '홍대입구역', true, true, 126.9221, 37.5572, '서울 마포구', '양화로', ST_SetSRID(ST_MakePoint(126.9221, 37.5572), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000001', 3, '잠실역', true, true, 127.1002, 37.5133, '서울 송파구', '올림픽로', ST_SetSRID(ST_MakePoint(127.1002, 37.5133), 4326), NOW(), NOW())
ON CONFLICT DO NOTHING;

-- 이벤트 2 출발지: 신촌, 건대입구, 이태원
INSERT INTO start_point (start_point_id, event_id, user_id, start_point_name, is_user, is_transit, road_longitude, road_latitude, address, road_address, point, created_at, modified_at)
VALUES
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000002', 1, '신촌역', true, true, 126.9366, 37.5554, '서울 서대문구', '신촌로', ST_SetSRID(ST_MakePoint(126.9366, 37.5554), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000002', 2, '건대입구역', true, true, 127.0694, 37.5403, '서울 광진구', '아차산로', ST_SetSRID(ST_MakePoint(127.0694, 37.5403), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000002', 3, '이태원역', true, true, 126.9942, 37.5346, '서울 용산구', '이태원로', ST_SetSRID(ST_MakePoint(126.9942, 37.5346), 4326), NOW(), NOW())
ON CONFLICT DO NOTHING;

-- 이벤트 3 출발지: 사당, 서울대입구, 수원
INSERT INTO start_point (start_point_id, event_id, user_id, start_point_name, is_user, is_transit, road_longitude, road_latitude, address, road_address, point, created_at, modified_at)
VALUES
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000003', 1, '사당역', true, true, 126.9816, 37.4766, '서울 동작구', '남부순환로', ST_SetSRID(ST_MakePoint(126.9816, 37.4766), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000003', 2, '서울대입구역', true, true, 126.9527, 37.4811, '서울 관악구', '남부순환로', ST_SetSRID(ST_MakePoint(126.9527, 37.4811), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000003', 3, '수원역', true, true, 127.0028, 37.2663, '경기 수원시', '덕영대로', ST_SetSRID(ST_MakePoint(127.0028, 37.2663), 4326), NOW(), NOW())
ON CONFLICT DO NOTHING;
