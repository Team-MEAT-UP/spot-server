import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, DEFAULT_HEADERS } from './common.js';

/**
 * [인원수별 스캔 벤치마크]
 * 2인 ~ 8인 케이스를 순차적으로 1회씩 실행하여
 * 알고리즘의 시간 복잡도에 따른 Latency 변화를 측정합니다.
 */
export const options = {
    vus: 1,
    iterations: 7, // 2인부터 8인까지 총 7개 케이스
};

const SCENARIOS = [
    { count: 2, id: 'aaaaaaaa-0000-0000-0000-000000000002' },
    { count: 3, id: 'aaaaaaaa-0000-0000-0000-000000000003' },
    { count: 4, id: 'aaaaaaaa-0000-0000-0000-000000000004' },
    { count: 5, id: 'aaaaaaaa-0000-0000-0000-000000000005' },
    { count: 6, id: 'aaaaaaaa-0000-0000-0000-000000000006' },
    { count: 7, id: 'aaaaaaaa-0000-0000-0000-000000000007' },
    { count: 8, id: 'aaaaaaaa-0000-0000-0000-000000000008' },
];

export default function () {
    const scenario = SCENARIOS[__ITER];
    const url = `${BASE_URL}/events/${scenario.id}`;

    const res = http.get(url, { 
        headers: DEFAULT_HEADERS,
        timeout: '60s' // 다익스트라 8인 케이스는 매우 오래 걸릴 수 있으므로 넉넉히 설정
    });

    const success = check(res, {
        'status is 200': (r) => r.status === 200,
    });

    if (success) {
        console.log(`[PASS] ${scenario.count} persons -> Response Time: ${res.timings.duration.toFixed(2)}ms`);
    } else {
        console.log(`[FAIL] ${scenario.count} persons -> Status: ${res.status}, Error: ${res.error}`);
    }
}
