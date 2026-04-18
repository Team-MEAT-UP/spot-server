/**
 * Scenario 2: Redis 캐싱 효과 검증 (Cache Aside / Write Around)
 *
 * 동일한 출발지-도착지 조합을 반복 요청하여 캐시 히트율 측정
 *   - 반복 요청 70% (캐시 히트 유도)
 *   - 신규 요청 30% (캐시 미스 유도)
 *
 * [Before] 캐시 없음 → 매 요청 ODsay API 호출
 *   SPRING_CACHE_TYPE=none 환경변수 추가 후 docker compose restart app
 *
 * [After] Redis 캐시 → 캐시 히트 시 외부 API 미호출
 *   기본 설정 (application-loadtest.yml)
 *
 * 실행:
 *   Before: k6 run --out json=results/s2_before.json scripts/scenario2-redis-cache.js
 *   After:  k6 run --out json=results/s2_after.json  scripts/scenario2-redis-cache.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { BASE_URL, DEFAULT_HEADERS, COMMON_THRESHOLDS, randomEventId, randomStationId, checkResponse } from './common.js';

// ── 커스텀 메트릭 ──
const routeDuration    = new Trend('route_duration', true);
const repeatRequestRate = new Rate('repeat_request_rate');

// 반복 요청에 사용할 고정 파라미터 (캐시 히트 유도)
const FIXED_EVENT_ID  = 'aaaaaaaa-0000-0000-0000-000000000001';
const FIXED_STATION_ID = 1;

export const options = {
  stages: [
    { duration: '1m', target: 10  },  // Warm-up
    { duration: '3m', target: 50  },  // 일반 부하
    { duration: '2m', target: 100 },  // 스파이크 부하
    { duration: '1m', target: 0   },  // Cool-down
  ],
  thresholds: {
    ...COMMON_THRESHOLDS,
    'route_duration': ['p(95)<2000', 'p(99)<5000'],
  },
};

export default function () {
  // 70% 반복 요청 (캐시 히트 유도), 30% 신규 요청 (캐시 미스)
  const isRepeat = Math.random() < 0.7;
  repeatRequestRate.add(isRepeat);

  const eventId   = isRepeat ? FIXED_EVENT_ID   : randomEventId();
  const stationId = isRepeat ? FIXED_STATION_ID : randomStationId();

  const url   = `${BASE_URL}/api/events/${eventId}/routes?stationId=${stationId}`;
  const start = Date.now();
  const res   = http.get(url, { headers: DEFAULT_HEADERS });
  const elapsed = Date.now() - start;

  routeDuration.add(elapsed);

  check(res, {
    'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'p95 < 2000ms': () => elapsed < 2000,
  });

  checkResponse(res, 'scenario2');

  // Redis INFO로 히트율 확인 방법 (측정 시 별도 터미널에서 실행):
  //   docker exec <redis-container> redis-cli INFO stats | grep keyspace_
  sleep(0.3);
}
