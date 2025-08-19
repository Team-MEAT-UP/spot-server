package com.meetup.server.subway.implement.processor;

import com.meetup.server.global.util.CoordinateUtil;
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
    private static final int TRANSFER_TIME_SEC = 180;
    // 직선 거리 1km당 평균 소요 시간 (초 단위, 2분/km)
    private static final int AVERAGE_TIME_SEC_PER_KM = 120;

    private final SubwayRepository subwayRepository;
    private final SubwayConnectionRepository subwayConnectionRepository;
    private final TransferInfoRepository transferInfoRepository;

    private Map<Integer, Subway> subwayMap;
    private Map<Integer, List<SubwayConnection>> connectionsMap;
    private Map<Integer, List<TransferInfo>> transfersMap;

    @PostConstruct
    public void init() {
        this.subwayMap = subwayRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Subway::getSubwayId, Function.identity()));

        this.connectionsMap = subwayConnectionRepository.findAllWithSubways()
                .stream()
                .collect(Collectors.groupingBy(conn -> conn.getFromSubway().getSubwayId()));

        this.transfersMap = transferInfoRepository.findAllWithSubways()
                .stream()
                .collect(Collectors.groupingBy(tr -> tr.getFromSubway().getSubwayId()));
    }

    /**
     * 출발역에서 도착역까지의 최단 경로를 탐색.
     * A* 알고리즘을 사용하여 최단 시간 경로를 찾으며, 노드 수 제한 시 대체 경로 반환.
     *
     * @param startSubwayId 출발역 ID
     * @param endSubwayId   도착역 ID
     * @return 최단 경로 결과 (SubwayPathResult) 또는 대체 경로
     */
    public SubwayPathResult findShortestPath(int startSubwayId, int endSubwayId) {
        PriorityQueue<SubwayPathNode> pathQueue = new PriorityQueue<>(
                Comparator.comparingDouble((SubwayPathNode node) -> node.totalTime() + heuristic(node.subwayId(), endSubwayId, subwayMap))
                        .thenComparingInt(node -> node.subwayIds().size())
        );
        pathQueue.offer(new SubwayPathNode(startSubwayId, 0, new ArrayList<>(List.of(startSubwayId)), 0));

        Map<String, Integer> visitedTimeMap = new HashMap<>();

        final int MAX_NODES = 500;
        final int MAX_TRANSFERS = 3;

        int nodeCount = 0;
        SubwayPathNode bestNode = null;

        while (!pathQueue.isEmpty()) {
            SubwayPathNode currentNode = pathQueue.poll();
            nodeCount++;

            if (bestNode == null || currentNode.totalTime() < bestNode.totalTime()) {
                bestNode = currentNode;
            }

            if (nodeCount >= MAX_NODES) {
                log.warn("[SubwayPathProcessor] : 노드 수 제한 초과로 대체 경로 반환 (startSubwayId: {}, endSubwayId: {})", startSubwayId, endSubwayId);
                return createFallbackPath(subwayMap, bestNode, startSubwayId, endSubwayId);
            }

            String pathKey = currentNode.subwayId() + "-" + currentNode.subwayIds().size();
            if (visitedTimeMap.containsKey(pathKey) && visitedTimeMap.get(pathKey) <= currentNode.totalTime()) {
                continue;
            }
            visitedTimeMap.put(pathKey, currentNode.totalTime());

            if (currentNode.subwayId() == endSubwayId) {
                List<String> pathNames = currentNode.subwayIds().stream()
                        .map(id -> subwayMap.get(id).getName())
                        .collect(Collectors.toList());
                return new SubwayPathResult(currentNode.totalTime(), currentNode.subwayIds(), pathNames);
            }

            // 현재 역에서 연결된 다음 역 탐색
            List<SubwayConnection> connections = connectionsMap.getOrDefault(currentNode.subwayId(), List.of());
            for (SubwayConnection connection : connections) {
                pathQueue.offer(new SubwayPathNode(
                        connection.getToSubway().getSubwayId(),
                        currentNode.totalTime() + connection.getSectionTimeSec(),
                        addSubwayToPath(currentNode.subwayIds(), connection.getToSubway().getSubwayId()),
                        currentNode.transferCount()
                ));
            }

            // 현재 역에서 가능한 환승 탐색
            List<TransferInfo> transfers = transfersMap.getOrDefault(currentNode.subwayId(), List.of());
            for (TransferInfo transfer : transfers) {
                if (currentNode.transferCount() >= MAX_TRANSFERS) {
                    continue;
                }
                pathQueue.offer(new SubwayPathNode(
                        transfer.getToSubway().getSubwayId(),
                        currentNode.totalTime() + TRANSFER_TIME_SEC,
                        addSubwayToPath(currentNode.subwayIds(), transfer.getToSubway().getSubwayId()),
                        currentNode.transferCount() + 1
                ));
            }
        }

        log.warn("[SubwayPathProcessor] : 경로를 찾지 못해 대체 경로 반환 (startSubwayId: {}, endSubwayId: {})", startSubwayId, endSubwayId);
        return createFallbackPath(subwayMap, bestNode, startSubwayId, endSubwayId);
    }

    /**
     * A* 알고리즘의 휴리스틱 함수.
     * 현재 역에서 도착역까지의 직선 거리 기반 예상 소요 시간(초) 계산.
     *
     * @param subwayId     현재 역 ID
     * @param endSubwayId  도착역 ID
     * @param subwayMap    역 정보 맵
     * @return 예상 소요 시간 (초)
     */
    private double heuristic(int subwayId, int endSubwayId, Map<Integer, Subway> subwayMap) {
        Subway start = subwayMap.get(subwayId);
        Subway end = subwayMap.get(endSubwayId);
        double distance = CoordinateUtil.calculateDistance(start.getPoint(), end.getPoint());
        return distance * AVERAGE_TIME_SEC_PER_KM;
    }

    /**
     * 최단 경로 탐색 실패 시 대체 경로 생성.
     * bestNode를 기반으로 경로를 만들고, 도착역 미도달 시 직선 거리 기반 시간 추가.
     *
     * @param subwayMap    역 정보 맵
     * @param bestNode     가장 유망한 노드 (최소 totalTime)
     * @param startSubwayId 출발역 ID
     * @param endSubwayId   도착역 ID
     * @return 대체 경로 결과
     */
    private SubwayPathResult createFallbackPath(Map<Integer, Subway> subwayMap, SubwayPathNode bestNode, int startSubwayId, int endSubwayId) {
        List<Integer> path;
        int estimatedTime;

        if (bestNode == null) {
            path = new ArrayList<>(List.of(startSubwayId, endSubwayId));
            estimatedTime = (int) heuristic(startSubwayId, endSubwayId, subwayMap);
        } else {
            path = new ArrayList<>(bestNode.subwayIds());
            estimatedTime = bestNode.totalTime();

            if (!path.getLast().equals(endSubwayId)) {
                path.add(endSubwayId);
                estimatedTime += (int) heuristic(bestNode.subwayId(), endSubwayId, subwayMap);
            }
        }

        List<String> pathNames = path.stream()
                .map(id -> subwayMap.get(id).getName())
                .collect(Collectors.toList());
        log.info("[SubwayPathProcessor] : 대체 경로 생성 - path: {}, totalTime: {}", path, estimatedTime);
        return new SubwayPathResult(estimatedTime, path, pathNames);
    }

    /**
     * 기존 경로에 새로운 역 ID를 추가하여 새 경로 생성.
     *
     * @param path     기존 경로 (역 ID 리스트)
     * @param subwayId 추가할 역 ID
     * @return 새 경로
     */
    private List<Integer> addSubwayToPath(List<Integer> path, int subwayId) {
        List<Integer> newPath = new ArrayList<>(path);
        newPath.add(subwayId);
        return newPath;
    }
}
