-- =====================================================
-- 부하 테스트용 시드 데이터 (init.sql) - 2~8인 스플릿 벤치마크용 (수정 버전)
-- =====================================================

-- ── 테스트 유저 (8명 확보) ──
INSERT INTO users (user_id, email, nickname, profile_image, social_id, personal_info_agreement, marketing_agreement, role, created_at, modified_at)
VALUES
  (1, 'loadtest1@moisam.kr', '부하테스터1', 'img1', 'social_1', true, true, 'USER', NOW(), NOW()),
  (2, 'loadtest2@moisam.kr', '부하테스터2', 'img2', 'social_2', true, true, 'USER', NOW(), NOW()),
  (3, 'loadtest3@moisam.kr', '부하테스터3', 'img3', 'social_3', true, true, 'USER', NOW(), NOW()),
  (4, 'loadtest4@moisam.kr', '부하테스터4', 'img4', 'social_4', true, true, 'USER', NOW(), NOW()),
  (5, 'loadtest5@moisam.kr', '부하테스터5', 'img5', 'social_5', true, true, 'USER', NOW(), NOW()),
  (6, 'loadtest6@moisam.kr', '부하테스터6', 'img6', 'social_6', true, true, 'USER', NOW(), NOW()),
  (7, 'loadtest7@moisam.kr', '부하테스터7', 'img7', 'social_7', true, true, 'USER', NOW(), NOW()),
  (8, 'loadtest8@moisam.kr', '부하테스터8', 'img8', 'social_8', true, true, 'USER', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- ── 테스트 이벤트 (2인~8인 케이스 생성) ──
INSERT INTO event (event_id, event_name, event_date_time, created_at, modified_at)
VALUES
  ('aaaaaaaa-0000-0000-0000-000000000002', '2인 테스트', '2026-05-01', NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000003', '3인 테스트', '2026-05-02', NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000004', '4인 테스트', '2026-05-03', NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000005', '5인 테스트', '2026-05-04', NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000006', '6인 테스트', '2026-05-05', NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000007', '7인 테스트', '2026-05-06', NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000008', '8인 테스트', '2026-05-07', NOW(), NOW())
ON CONFLICT (event_id) DO NOTHING;

-- ── 출발지 데이터 (누적 배정) ──
INSERT INTO start_point (start_point_id, event_id, user_id, start_point_name, is_user, is_transit, road_longitude, road_latitude, address, road_address, point, created_at, modified_at)
VALUES
  -- 2인 케이스
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000002', 1, '강남', true, true, 127.0276, 37.4979, '서울 강남구', '강남대로', ST_SetSRID(ST_MakePoint(127.0276, 37.4979), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000002', 2, '홍대', true, true, 126.9221, 37.5572, '서울 마포구', '양화로', ST_SetSRID(ST_MakePoint(126.9221, 37.5572), 4326), NOW(), NOW()),
  
  -- 8인 케이스 (최종 부하 타겟)
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000008', 1, '강남', true, true, 127.0276, 37.4979, '서울 강남구', '강남대로', ST_SetSRID(ST_MakePoint(127.0276, 37.4979), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000008', 2, '홍대', true, true, 126.9221, 37.5572, '서울 마포구', '양화로', ST_SetSRID(ST_MakePoint(126.9221, 37.5572), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000008', 3, '잠실', true, true, 127.1002, 37.5133, '서울 송파구', '올림픽로', ST_SetSRID(ST_MakePoint(127.1002, 37.5133), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000008', 4, '신촌', true, true, 126.9366, 37.5554, '서울 서대문구', '신촌로', ST_SetSRID(ST_MakePoint(126.9366, 37.5554), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000008', 5, '건대', true, true, 127.0694, 37.5403, '서울 광진구', '아차산로', ST_SetSRID(ST_MakePoint(127.0694, 37.5403), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000008', 6, '사당', true, true, 126.9816, 37.4766, '서울 동작구', '남부순환로', ST_SetSRID(ST_MakePoint(126.9816, 37.4766), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000008', 7, '노량진', true, true, 126.9426, 37.5135, '서울 동작구', '노량진로', ST_SetSRID(ST_MakePoint(126.9426, 37.5135), 4326), NOW(), NOW()),
  (gen_random_uuid(), 'aaaaaaaa-0000-0000-0000-000000000008', 8, '왕십리', true, true, 127.0374, 37.5615, '서울 성동구', '왕십리로', ST_SetSRID(ST_MakePoint(127.0374, 37.5615), 4326), NOW(), NOW())
ON CONFLICT DO NOTHING;
