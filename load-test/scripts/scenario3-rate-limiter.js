/**
 * Scenario 3: Rate Limiter (Lua Script 기반 원자적 처리)
 *
 * ODsay / Kakao API 일일 호출 한도 Rate Limiter 동작 검증
 *   → 멀티 스레드 환경에서 Race Condition 없이 원자적 카운팅 확인
 *   → 한도 임박(90%) / 초과(100%+) 시 Fallback 처리 확인
 *   → Discord 알림 발송 시점 확인
 *
 * [Before] Rate Limit 없음 → 한도 초과 시 외부 API 직접 에러
 * [After]  Lua Script Rate Limiter → Fallback 응답 + Discord 알림
 *
 * 실행:
 *   Before: k6 run --out json=results/s3_before.json scripts/scenario3-rate-limiter.js
 *   After:  k6 run --out json=results/s3_after.json  scripts/scenario3-rate-limiter.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import { BASE_URL, DEFAULT_HEADERS, randomEventId, checkResponse } from './common.js';

// ── 커스텀 메트릭 ──
const rateLimitedCount  = new Counter('rate_limited_responses');  // 429 응답 수
const fallbackCount     = new Counter('fallback_responses');      // Fallback 응답 수
const errorRate         = new Rate('error_rate');

export const options = {
  stages: [
    { duration: '30s', target: 30 },  // Phase 1: 한도 90% 수준 도달
    { duration: '30s', target: 60 },  // Phase 2: 한도 초과 유도 (Race Condition 발생 여부 확인)
    { duration: '30s', target: 0  },  // Cool-down
  ],
  thresholds: {
    'error_rate':          ['rate<0.01'],  // After: Fallback으로 1% 미만
    'rate_limited_responses': ['count>=0'], // Before: 폭증 예상, After: 0 기대
  },
};

export default function () {
  const eventId = randomEventId();
  const url     = `${BASE_URL}/api/events/${eventId}/meeting-points`;

  const res = http.post(url, null, { headers: DEFAULT_HEADERS });

  // 429 Too Many Requests → Rate Limit 초과
  if (res.status === 429) {
    rateLimitedCount.add(1);
  }

  // Fallback 응답 판단 (서버에서 X-Fallback: true 헤더 반환 시)
  if (res.headers['X-Fallback'] === 'true') {
    fallbackCount.add(1);
  }

  const isError = res.status >= 500;
  errorRate.add(isError);

  check(res, {
    'not 500 error (fallback 처리됨)': (r) => r.status < 500,
    'response time < 3000ms':          (r) => r.timings.duration < 3000,
  });

  checkResponse(res, 'scenario3');
  sleep(0.1); // 빠른 연속 요청으로 Race Condition 유발
}
