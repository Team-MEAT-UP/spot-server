package com.meetup.server.subway.implement.processor;

import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.domain.SubwayConnection;
import com.meetup.server.subway.domain.TransferInfo;
import com.meetup.server.subway.infrastructure.jpa.SubwayConnectionRepository;
import com.meetup.server.subway.infrastructure.jpa.SubwayRepository;
import com.meetup.server.subway.infrastructure.jpa.TransferInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubwayPathProcessor {

    // 환승에 소요되는 고정 시간 (초 단위, 3분)
    private static final int DEFAULT_TRANSFER_DELAY = 180;
    private static final int UNREACHABLE_TIME = Integer.MAX_VALUE / 2;

    private final SubwayRepository subwayRepository;
    private final SubwayConnectionRepository subwayConnectionRepository;
    private final TransferInfoRepository transferInfoRepository;

    private Map<Integer, Subway> subwayMap;

    // 인덱스 변환용 맵 및 배열
    private Map<Integer, Integer> subwayIdToIndex;
    private int[] indexToSubwayId;

    // 플로이드-워셜 결과 저장 배열
    private int[][] shortestTime; // [출발][도착] = 최소 소요 시간
    private int[][] nextNode; // [출발][도착] = 다음 역

    /**
     * O(V^3): 스프링 서버 기동 시 모든 지하철 역 간의 '최단 경로(All-Pairs Shortest Path)'를 미리 계산
     * 이후 출발 <-> 중간지점 조회 시 O(1) 반환
     */
    @PostConstruct
    public void init() {
        this.subwayMap = subwayRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Subway::getSubwayId, Function.identity()));

        // 1. 역 ID를 연속된 배열 인덱스(0 ~ N-1)로 변환
        List<Integer> subwayIds = new ArrayList<>(subwayMap.keySet());
        Collections.sort(subwayIds);
        int totalStationCount = subwayIds.size();
        subwayIdToIndex = new HashMap<>();
        indexToSubwayId = new int[totalStationCount];
        for (int i = 0; i < totalStationCount; i++) {
            subwayIdToIndex.put(subwayIds.get(i), i);
            indexToSubwayId[i] = subwayIds.get(i);
        }

        // 2. 2차원 거리 행렬과 다음 노드 행렬을 초기화 (가장 큰 값인 UNREACHABLE_TIME, 경로는 -1 로 설정)
        shortestTime = new int[totalStationCount][totalStationCount];
        nextNode = new int[totalStationCount][totalStationCount];
        for (int[] row : shortestTime) Arrays.fill(row, UNREACHABLE_TIME);
        for (int[] row : nextNode) Arrays.fill(row, -1);

        // 내 위치에서 내 위치로 가는 시간은 0초
        for (int i = 0; i < totalStationCount; i++) {
            shortestTime[i][i] = 0;
            nextNode[i][i] = i;
        }

        // 3. 역과 역 사이의 소요 시간을 저장
        for (SubwayConnection connection : subwayConnectionRepository.findAllWithSubways()) {
            Integer fromIdx = subwayIdToIndex.get(connection.getFromSubway().getSubwayId());
            Integer toIdx = subwayIdToIndex.get(connection.getToSubway().getSubwayId());
            if (fromIdx == null || toIdx == null) continue;

            int time = connection.getSectionTimeSec();
            if (time < shortestTime[fromIdx][toIdx]) {
                shortestTime[fromIdx][toIdx] = time;
                nextNode[fromIdx][toIdx] = toIdx;
            }
        }

        // 4. 환승역 간의 도보 이동 지연 시간(DEFAULT_TRANSFER_DELAY)을 저장
        for (TransferInfo transferInfo : transferInfoRepository.findAllWithSubways()) {
            Integer fromIdx = subwayIdToIndex.get(transferInfo.getFromSubway().getSubwayId());
            Integer toIdx = subwayIdToIndex.get(transferInfo.getToSubway().getSubwayId());
            if (fromIdx == null || toIdx == null) continue;

            if (DEFAULT_TRANSFER_DELAY < shortestTime[fromIdx][toIdx]) {
                shortestTime[fromIdx][toIdx] = DEFAULT_TRANSFER_DELAY;
                nextNode[fromIdx][toIdx] = toIdx;
            }
        }

        // 5. Floyd-Warshall
        // departIdx(출발역)에서 arriveIdx(도착역)로 직접 가는 것보다, viaIdx(경유역)를 거쳐가는 것이 더 빠를 경우 거리를 갱신
        long startTime = System.currentTimeMillis();

        for (int viaIdx = 0; viaIdx < totalStationCount; viaIdx++) {
            for (int departIdx = 0; departIdx < totalStationCount; departIdx++) {
                if (shortestTime[departIdx][viaIdx] == UNREACHABLE_TIME) continue;
                for (int arriveIdx = 0; arriveIdx < totalStationCount; arriveIdx++) {
                    if (shortestTime[viaIdx][arriveIdx] == UNREACHABLE_TIME) continue;

                    int newTime = shortestTime[departIdx][viaIdx] + shortestTime[viaIdx][arriveIdx];
                    if (newTime < shortestTime[departIdx][arriveIdx]) {
                        shortestTime[departIdx][arriveIdx] = newTime;
                        nextNode[departIdx][arriveIdx] = nextNode[departIdx][viaIdx];
                    }
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Floyd-Warshall 길찾기 전처리 계산 완료 (총 역 수: {}, 소요시간: {}ms)", totalStationCount, elapsed);
    }

    /**
     * 출발역에서 도착역까지의 최단 경로(시간 및 거쳐간 노드 리스트)를 반환합니다.
     * 메모리에 2차원 배열로 계산되어 있으므로, 탐색 비용 O(1)과 경로 복원 비용 O(K)만 소모됩니다.
     *
     * @param startSubwayId 출발역 DB PK
     * @param endSubwayId   도착역 DB PK
     * @return 최단 경로 결과 객체 (경로가 끊긴 곳이라면 null을 반환)
     */
    public SubwayPathResult findShortestPath(int startSubwayId, int endSubwayId) {
        Integer startIdx = subwayIdToIndex.get(startSubwayId);
        Integer endIdx = subwayIdToIndex.get(endSubwayId);

        if (startIdx == null || endIdx == null || nextNode[startIdx][endIdx] == -1) {
            log.warn("[유효하지 않은 경로 데이터] 출발역ID={} → 도착역ID={}", startSubwayId, endSubwayId);
            return null;
        }

        int totalTime = shortestTime[startIdx][endIdx];

        List<Integer> path = new ArrayList<>();
        int current = startIdx;

        while (current != endIdx) {
            path.add(indexToSubwayId[current]);
            current = nextNode[current][endIdx];

            if (current == -1) {
                log.warn("[경로 복원 실패] 출발역ID={} → 도착역ID={}", startSubwayId, endSubwayId);
                return null;
            }
        }
        path.add(indexToSubwayId[endIdx]);

        List<String> pathNames = path.stream()
                .map(id -> subwayMap.get(id).getName())
                .toList();

        return new SubwayPathResult(totalTime, path, pathNames);
    }
}
