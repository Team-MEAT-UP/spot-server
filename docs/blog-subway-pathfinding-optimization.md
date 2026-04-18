# 🚇 [기술 블로그] 지하철 중간지점 탐색 알고리즘 고도화 및 성능 개선기

모임 장소를 정할 때 가장 중요한 것은 **"모두에게 공평하고 접근성이 좋은 위치"**를 찾는 것입니다. 우리 프로젝트에서는 다수의 참가자가 출발하는 위치에서 가장 공평하고 시간이 적게 걸리는 '지하철 중간지점'을 추천해주는 기능을 제공합니다.

하지만 프로토타입 수준의 초기 구현에서는 여러 성능적, 논리적 한계에 직면했습니다. 이번 글에서는 어떻게 기존 알고리즘의 문제점을 파악하고, 시간 복잡도를 줄임과 동시에 추천의 질을 높였는지에 대한 과정을 공유합니다.

---

## 🚨 문제 인식: 기존 A* 알고리즘의 한계

기존 지하철 최단 경로 탐색 시스템은 `A* 알고리즘` 기반으로 동작하고 있었습니다. 하지만 이 구조는 운영 환경에서 치명적인 몇 가지 단점이 존재했습니다.

1. **과도한 반복 연산**: 다중 출발지에서 여러 후보 지하철역까지 탐색해야 하므로, `참가자 수 × 후보 역 수`만큼 O(E log V)의 A* 탐색이 반복 실행되었습니다.
2. **휴리스틱의 한계(과대추정)**: 지하철 노선이 직선이 아님에도 `직선거리 거리 × 120s/km` 공식을 휴리스틱으로 사용하여 최적의 해를 보장하지 못했습니다. (A*의 Admissibility 위반)
3. **노드 강제 컷오프(MAX_NODES=500)**: 성능을 위해 거리가 먼 경우 일정 노드 수 이상 탐색하면 강제로 중단하였고, 이로 인해 부정확한 대체(Fallback) 경로가 반환되곤 했습니다.
4. **부정확한 후보군 선택 키 버그**: `pathKey`를 경로의 개수로만 두어, 우회하더라도 시간이 더 짧거나 환승이 적은 최적의 경로 자체를 덮어쓰거나 찾지 못하는 버그가 존재했습니다.

### (Before) 기존 A* 탐색 및 비효율적인 분기 처리
```java
// 기존 SubwayPathProcessor (A* 알고리즘)
public SubwayPathResult findShortestPath(int startSubwayId, int endSubwayId) {
    PriorityQueue<SubwayPathNode> pathQueue = new PriorityQueue<>(
        Comparator.comparingInt(SubwayPathNode::totalTime)
            .thenComparingInt(node -> node.subwayIds().size())
    );
    Map<String, Integer> visitedTimeMap = new HashMap<>(); // 키가 부정확하게 매핑될 위험

    // ...초기 세팅 후 큐 순회...
    while (!pathQueue.isEmpty() && nodesExplored < MAX_NODES) {
        SubwayPathNode currentNode = pathQueue.poll();
        
        // 탐색 노드가 초과되면 강제로 Fallback(부정확한 경로) 생성
        if (nodesExplored >= MAX_NODES) {
            return createFallbackPath(startSubwayId, endSubwayId);
        }
        // ...수많은 조건 검사와 반복...
    }
}
```

---

## 💡 해결책 1: Floyd-Warshall 알고리즘으로의 대전환

지하철역의 개수는 수백 개로 고정적이며 제한적이라는 도메인 특성이 있습니다. 이를 십분 활용하여 **서버 시작(초기화) 시점에 모든 역 쌍 간의 최단 시간을 미리 계산(Pre-compute)하는 Floyd-Warshall 알고리즘**을 도입했습니다.

### 도입 효과
- **시간 복잡도 혁신**: 기존 요청마다 돌던 A* 알고리즘을 걷어내어 경로 탐색 복잡도를 **O(1) (경로 복원 시 O(경로길이))**로 드라마틱하게 줄였습니다. 
- 애플리케이션 프로비저닝 시 초기화(`@PostConstruct`) 부하를 단 한 번(약 2~3초)만 지불하면 됩니다.
- 부정확한 Fallback 로직 및 강제 종료 로직을 걷어내고, 항상 최적해가 보장된 실제 갈 수 있는 경로만 깨끗하게 응답받을 수 있습니다. 환승 횟수 제약(MAX_TRANSFERS)도 불필요해져 제거했습니다.

### (After) 전역 최단 비용 사전 계산 및 O(1) 조회
```java
// 개선된 SubwayPathProcessor (Floyd-Warshall)
@PostConstruct
public void init() {
    // 1. 모든 역, 단순 연결, 환승 간선 기반 가중치 2D 배열 초기화
    // 2. Floyd-Warshall 3중 루프 실행하여 모든 O(V^3) 쌍 간 최단 시간/루트 계산 완료
    for (int k = 0; k < numStations; k++) {
        for (int i = 0; i < numStations; i++) {
            for (int j = 0; j < numStations; j++) {
                if (dist[i][k] + dist[k][j] < dist[i][j]) {
                    dist[i][j] = dist[i][k] + dist[k][j];
                    next[i][j] = next[i][k]; // 경로 복원 포인터
                }
            }
        }
    }
}

public SubwayPathResult findShortestPath(int startSubwayId, int endSubwayId) {
    // 요청 시 단순히 메모리에서 캐싱된 최단 시간을 O(1) 수준으로 꺼내고 연결 역만 조립
    int startIdx = idToIndex.get(startSubwayId);
    int endIdx = idToIndex.get(endSubwayId);
    
    if (dist[startIdx][endIdx] == INF) return null; // 완벽하게 도달 유무 파악
    
    // next 배열을 통해 도착지까지의 경로 재구성 완료
    return buildPathResult(startIdx, endIdx); 
}
```

---

## 💡 해결책 2: 추천 역 탐색 범위와 중심점 정밀도 향상

최단 시간을 완벽하게 구할 수 있게 됨에 따라, 물리적인 후보를 선정하는 로직도 함께 개선할 수 있었습니다.

### 중심점 계산 로직 (Geometric Median) 도입
기존에는 여러 출발지의 위도/경도를 단순 산술 평균(Arithmetic Mean)으로 도출했습니다. 하지만 이는 한 명이 동떨어진 곳에 있을 경우 중심이 극단적으로 끌려가는 문제가 있습니다.

이를 위해 **Weiszfeld 알고리즘 기반의 Geometric Median(기하 중앙값)** 계산 방식을 적용했습니다. 모든 참가자로부터의 거리 합이 최소가 되는 지점을 반복적으로 수렴하여 도출하게 되므로, 소수의 극단적인 좌표에 중심점이 휘둘리지 않고 균형감 있는 중간 지점을 도출해냅니다.

### 지하철역 탐색 반경 확대
경로 조회 연산이 O(1)로 줄었으므로 후보역을 여유 있게 잡아도 성능 저하가 없습니다.
중심점을 기준으로 기준 반경 1.5km에서 역이 하나라도 나오면 바로 반환하던 로직을 지우고, **최대 10km 이내에서 최소 5개 이상의 역이 확보될 때까지** 탐색 반경을 넓히도록 수정했습니다.

```java
// (Before) 기존 탐색: 하나라도 역이 나오면 즉시 반환 (반경 최대 5km)
if (!nearbySubways.isEmpty()) return nearbySubways; 

// (After) 변경 탐색: 더 정교한 타당성 범위를 잡아 최소 5개가 나올 때까지 탐색 (반경 최대 10km)
if (nearbySubways.size() >= 5) return nearbySubways;
```

---

## 💡 해결책 3: 공정성(Fairness) 비즈니스 로직 고도화

마지막 관문은 선정된 후보 역들 중에서 **가장 공평한 역**을 줄 세우는 `findTopFairSubways()` 로직이었습니다.

### 이전의 함정: 표준 편차(StdDev)의 역설
과거에는 가장 공평한 곳을 찾기 위해 참가자 간의 도달 시간 **"표준 편차"**만 확인했습니다. 그 결과, 다음과 같은 심각한 오류가 발생했습니다.

* 후보 역 1: A도 1시간 소요 / B도 1시간 소요 -> `편차 0`
* 후보 역 2: A는 20분 소요 / B는 25분 소요 -> `편차 150 (차이 5분)`

단순 표준 편차만 보면 편차가 0인 "후보 역 1"이 1위로 선정되겠지만, 실제 사용자 관점에서는 왕복 2시간을 허비하는 최악의 중간 지점이 됩니다.

### 복합 점수 (Composite Score) 방식 적용
단순히 편차만 보지 않고, **공정성(표준편차)과 효율성(평균 소요시간)을 가중 합산하는 복합 점수 모델**을 도입했습니다.

> **개선된 Score = (0.6 × 표준편차) + (0.4 × 평균 소요시간)**

해당 공식을 통해 편차가 약간 있더라도 전체 소요시간이 훨씬 적은 후보가 상단에 노출되도록 하여 체감 만족도를 크게 끌어올렸습니다.

### (Before/After) 복합 점수를 통한 정렬 기준 변경
```java
// (Before) 단순 표준편차 비교 (낮은 순서대로 정렬)
return validCandidates.entrySet().stream()
    .sorted(Comparator.comparingDouble(entry -> calculateStandardDeviation(entry.getValue())))
    .limit(TOP_N)
    .toList();

// (After) 가중치 합산을 통한 복합 점수 평가
private static final double FAIRNESS_WEIGHT = 0.6;
private static final double EFFICIENCY_WEIGHT = 0.4;

private double calculateFairnessScore(List<Integer> times) {
    double stdDev = calculateStandardDeviation(times);
    double avg = times.stream().mapToInt(i -> i).average().orElse(0);
    return FAIRNESS_WEIGHT * stdDev + EFFICIENCY_WEIGHT * avg;
}

// 스트림 비교 조건에 새로운 메트릭 적용
return validCandidates.entrySet().stream()
    .sorted(Comparator.comparingDouble(entry -> calculateFairnessScore(entry.getValue())))
    //...
```

---

## 🎯 결론 및 시사점

알고리즘 교체 단계에서 기존 도메인의 특성(노드 수가 제한적인 지하철 시스템)을 파악하고 Pre-computation 방식을 적용함으로써 성능 이슈를 근본적으로 해소했습니다. 성능 병목이 풀림에 따라 탐색 역에 대한 반경을 넓히고 휴리스틱 오차 및 Fallback 로직을 제거할 수 있었으며, 이를 바탕으로 추천 로직 자체를 복합 점수를 사용하는 방향으로 고도화할 수 있었습니다.

결과적으로 훨씬 견고하고 빠르며, 사용자 체감 품질을 대폭 상승시킨 추천 시스템을 구축할 수 있었습니다. 이번 경험을 통해 백엔드에서의 최적의 알고리즘 선택이 최종 사용자의 만족도(UX)에 어떻게 직결되는지 크게 체감할 수 있었습니다.
