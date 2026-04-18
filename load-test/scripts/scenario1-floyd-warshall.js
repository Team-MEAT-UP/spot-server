/**
 * Scenario 1: 중간지점 후보군 탐색 (Floyd-Warshall O(1) 캐싱)
 *
 * [Before] Redis 캐시 비활성화 상태 → 매 요청마다 경로 재계산
 *   spring.cache.type=none 또는 app.route.cache.enabled=false 로 배포
 *
 * [After] Redis 캐시 활성화 → O(1) 조회
 *   기본 설정 그대로 (application-loadtest.yml 기준)
 *
 * 실행:
 *   Before: k6 run --out json=results/s1_before.json scripts/scenario1-floyd-warshall.js
 *   After:  k6 run --out json=results/s1_after.json  scripts/scenario1-floyd-warshall.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { BASE_URL, DEFAULT_HEADERS, COMMON_THRESHOLDS, randomEventId, checkResponse } from './common.js';

// ── 커스텀 메트릭 ──
const meetingPointDuration = new Trend('meeting_point_duration', true);
const cacheHitCount        = new Counter('cache_hit_count');
const cacheMissCount       = new Counter('cache_miss_count');

export const options = {
  stages: [
    { duration: '1m', target: 10  },  // Warm-up (JIT 컴파일 대기)
    { duration: '3m', target: 50  },  // 일반 부하
    { duration: '2m', target: 100 },  // 스파이크 부하
    { duration: '1m', target: 0   },  // Cool-down
  ],
  thresholds: {
    ...COMMON_THRESHOLDS,
    'meeting_point_duration': ['p(95)<2000', 'p(99)<5000'],
  },
};

export default function () {
  const eventId = randomEventId();
  const url     = `${BASE_URL}/api/events/${eventId}/meeting-points`;

  const start = Date.now();
  const res   = http.post(url, null, { headers: DEFAULT_HEADERS });
  const elapsed = Date.now() - start;

  meetingPointDuration.add(elapsed);

  // X-Cache-Status 헤더로 캐시 히트/미스 판단 (서버 측 헤더 추가 시)
  const cacheStatus = res.headers['X-Cache-Status'];
  if (cacheStatus === 'HIT') {
    cacheHitCount.add(1);
  } else {
    cacheMissCount.add(1);
  }

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response has coordinate': (r) => r.json('coordinate') !== undefined,
    'response has popularity': (r) => r.json('popularity') !== undefined,
    'p95 < 2000ms': () => elapsed < 2000,
  });

  checkResponse(res, 'scenario1');
  sleep(0.5);
}
