/**
 * k6 공용 설정 모듈
 * 모든 시나리오 스크립트에서 import해서 사용
 *
 * 실행 예시:
 *   BASE_URL=http://<GCP-외부-IP>:8080 k6 run --out json=results/s1_before.json scripts/scenario1-floyd-warshall.js
 */

// ────────────────────────────────────────────────────────────────
// ⚙️  환경 설정 (변경 필요)
// ────────────────────────────────────────────────────────────────
export const BASE_URL = __ENV.BASE_URL || 'http://<GCP-외부-IP>:18080';

/** k6 공용 HTTP 헤더 */
export const DEFAULT_HEADERS = {
  'Content-Type': 'application/json',
  'Accept':       'application/json',
};

/** Accept-Encoding 포함 헤더 (Scenario 5 Gzip용) */
export const GZIP_HEADERS = {
  ...DEFAULT_HEADERS,
  'Accept-Encoding': 'gzip, deflate',
};

// ────────────────────────────────────────────────────────────────
// 📊 공통 thresholds (각 시나리오에서 필요시 오버라이드)
// ────────────────────────────────────────────────────────────────
export const COMMON_THRESHOLDS = {
  http_req_failed:   ['rate<0.01'],      // Error Rate < 1%
  http_req_duration: ['p(95)<2000'],     // p95 < 2,000ms
};

// ────────────────────────────────────────────────────────────────
// 🔑 테스트 데이터 (init.sql과 동기화 필요)
// ────────────────────────────────────────────────────────────────
export const TEST_DATA = {
  // init.sql에 미리 삽입한 이벤트 UUID 목록
  eventIds: [
    'aaaaaaaa-0000-0000-0000-000000000001',
    'aaaaaaaa-0000-0000-0000-000000000002',
    'aaaaaaaa-0000-0000-0000-000000000003',
  ],

  // 지하철역 ID 목록 (subway 테이블 기준)
  stationIds: [1, 2, 3, 4, 5],
};

/** 랜덤 이벤트 ID 반환 */
export function randomEventId() {
  const ids = TEST_DATA.eventIds;
  return ids[Math.floor(Math.random() * ids.length)];
}

/** 랜덤 지하철역 ID 반환 */
export function randomStationId() {
  const ids = TEST_DATA.stationIds;
  return ids[Math.floor(Math.random() * ids.length)];
}

/** 응답 성공 여부 확인 헬퍼 */
export function checkResponse(res, tag) {
  if (res.status !== 200) {
    console.error(`[${tag}] status=${res.status} body=${res.body.substring(0, 200)}`);
  }
}
