/**
 * Scenario 5: Gzip 커스텀 CompressFilter
 *
 * 대용량 경로 JSON 응답에 Gzip 압축 적용 효과 검증
 *   → 응답 페이로드 크기 70~80% 감소 확인
 *   → 네트워크 전송 시간 감소 확인
 *   → 서버 CPU 오버헤드 확인 (Pinpoint에서 확인)
 *
 * [Before] CompressFilter Bean 미등록 → 압축 없이 전송
 *   CompressFilter @Component 제거 후 docker compose restart app
 *
 * [After] CompressFilter 등록 → Gzip 압축 전송
 *   기본 설정
 *
 * 실행:
 *   Before: k6 run --out json=results/s5_before.json scripts/scenario5-gzip.js
 *   After:  k6 run --out json=results/s5_after.json  scripts/scenario5-gzip.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { BASE_URL, GZIP_HEADERS, DEFAULT_HEADERS, COMMON_THRESHOLDS, randomEventId, checkResponse } from './common.js';

// ── 커스텀 메트릭 ──
const responseSizeBytes  = new Trend('response_size_bytes');
const compressedCount    = new Counter('gzip_compressed_responses');
const uncompressedCount  = new Counter('gzip_uncompressed_responses');
const transferDuration   = new Trend('transfer_duration', true);

export const options = {
  stages: [
    { duration: '1m', target: 10  },  // Warm-up
    { duration: '2m', target: 50  },  // 일반 부하
    { duration: '3m', target: 100 },  // 스파이크 부하
    { duration: '1m', target: 0   },  // Cool-down
  ],
  thresholds: {
    ...COMMON_THRESHOLDS,
    'transfer_duration': ['p(95)<2000'],
    // After: 응답 크기가 Before 대비 70% 이상 감소해야 함 (수동 확인)
  },
};

export default function () {
  const eventId = randomEventId();
  const url     = `${BASE_URL}/api/events/${eventId}/meeting-points`;

  // Accept-Encoding: gzip 포함 요청
  const start = Date.now();
  const res   = http.post(url, null, { headers: GZIP_HEADERS });
  const elapsed = Date.now() - start;

  transferDuration.add(elapsed);

  // 응답 크기 측정
  const bodyLength = res.body ? res.body.length : 0;
  responseSizeBytes.add(bodyLength);

  // Gzip 압축 여부 확인 (Content-Encoding 헤더)
  const isGzipped = res.headers['Content-Encoding'] === 'gzip';
  if (isGzipped) {
    compressedCount.add(1);
  } else {
    uncompressedCount.add(1);
  }

  check(res, {
    'status is 200':           (r) => r.status === 200,
    'gzip 압축 적용됨 (After)': ()  => isGzipped,
    'p95 < 2000ms':            ()  => elapsed < 2000,
  });

  checkResponse(res, 'scenario5');

  // 페이로드 크기 로그 (처음 10번 요청)
  if (__ITER < 10) {
    console.log(`[s5] body=${bodyLength}B | gzip=${isGzipped} | time=${elapsed}ms`);
  }

  sleep(0.5);
}
