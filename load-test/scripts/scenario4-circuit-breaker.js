/**
 * Scenario 4: Resilience4j (Retry / Circuit Breaker / Timeout)
 *
 * WireMock으로 외부 API 장애를 시뮬레이션하여 Resilience4j 동작 검증
 *
 * 사전 준비:
 *   1. GCP VM에서 WireMock 실행 확인:
 *      docker compose ps wiremock
 *
 *   2. WireMock Stub 설정 (Phase별):
 *
 *      [Phase 1 - 정상]:
 *      curl -X POST http://<GCP-IP>:9090/__admin/mappings \
 *        -d '{"request":{"method":"ANY","urlPattern":".*"},
 *             "response":{"status":200,"body":"{}","fixedDelayMilliseconds":500}}'
 *
 *      [Phase 2 - 지연 (5s)]:
 *      curl -X POST http://<GCP-IP>:9090/__admin/mappings \
 *        -d '{"request":{"method":"ANY","urlPattern":".*"},
 *             "response":{"status":200,"body":"{}","fixedDelayMilliseconds":5000}}'
 *
 *      [Phase 3 - 100% 오류]:
 *      curl -X POST http://<GCP-IP>:9090/__admin/mappings \
 *        -d '{"request":{"method":"ANY","urlPattern":".*"},
 *             "response":{"status":503,"body":"Service Unavailable"}}'
 *
 * 실행:
 *   PHASE=1 k6 run --out json=results/s4_phase1.json scripts/scenario4-circuit-breaker.js
 *   PHASE=2 k6 run --out json=results/s4_phase2.json scripts/scenario4-circuit-breaker.js
 *   PHASE=3 k6 run --out json=results/s4_phase3.json scripts/scenario4-circuit-breaker.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter, Rate } from 'k6/metrics';
import { BASE_URL, DEFAULT_HEADERS, randomEventId, randomStationId, checkResponse } from './common.js';

const phase = parseInt(__ENV.PHASE || '1');

// ── 커스텀 메트릭 ──
const routeDuration     = new Trend('route_duration_cb', true);
const circuitOpenCount  = new Counter('circuit_open_responses');
const fallbackCount     = new Counter('fallback_count');
const errorRate         = new Rate('error_rate_cb');

export const options = {
  stages: phase === 1
    ? [
        { duration: '1m', target: 20 },  // Phase 1: 정상 응답 Baseline
        { duration: '1m', target: 0  },
      ]
    : phase === 2
    ? [
        { duration: '2m', target: 30 },  // Phase 2: 5s 지연 → Timeout + Retry 확인
        { duration: '1m', target: 0  },
      ]
    : [
        { duration: '2m', target: 30 },  // Phase 3: 100% 오류 → Circuit Open 확인
        { duration: '1m', target: 0  },
      ],

  thresholds: {
    'error_rate_cb': ['rate<0.01'],
    // Phase 2: Timeout 적용으로 p99 < 3000ms (5s 지연이지만 1.5s timeout)
    'route_duration_cb': phase === 2
      ? ['p(99)<3000']
      : ['p(95)<2000'],
  },
};

export default function () {
  const eventId   = randomEventId();
  const stationId = randomStationId();
  const url       = `${BASE_URL}/api/events/${eventId}/routes?stationId=${stationId}`;

  const start = Date.now();
  const res   = http.get(url, {
    headers: DEFAULT_HEADERS,
    timeout: '10s',  // k6 레벨 타임아웃 (서버 Resilience4j timeout과 별개)
  });
  const elapsed = Date.now() - start;

  routeDuration.add(elapsed);

  // Circuit Breaker Open 감지 (503 또는 X-Circuit-State: OPEN 헤더)
  if (res.status === 503 || res.headers['X-Circuit-State'] === 'OPEN') {
    circuitOpenCount.add(1);
  }

  // Fallback 응답 감지
  if (res.headers['X-Fallback'] === 'true') {
    fallbackCount.add(1);
  }

  const isError = res.status >= 500 && res.headers['X-Fallback'] !== 'true';
  errorRate.add(isError);

  check(res, {
    'not hard error (fallback or cb open)': (r) => r.status < 500 || r.headers['X-Fallback'] === 'true',
    'response within timeout':              () => elapsed < 3500,
  });

  checkResponse(res, `scenario4-phase${phase}`);
  sleep(0.5);
}
