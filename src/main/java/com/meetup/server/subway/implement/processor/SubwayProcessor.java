package com.meetup.server.subway.implement.processor;

import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.implement.reader.SubwayReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubwayProcessor {

    private static final int MINIMUM_PEOPLE_REQUIRED = 2;
    private static final int MAX_SUBWAY_COUNT = 3;
    private static final double FAIRNESS_WEIGHT = 0.6;
    private static final double EFFICIENCY_WEIGHT = 0.4;

    private final SubwayReader subwayReader;
    private final SubwayPathProcessor subwayPathProcessor;

    public List<Integer> findTopFairSubways(Map<StartPoint, List<SubwayPathResult>> startPointToSubwayPaths) {
        Map<Integer, List<Integer>> destinationSubwayTimeMap = new HashMap<>();

        startPointToSubwayPaths.forEach((startPoint, subwayPaths) ->
                subwayPaths.forEach(subwayPath -> {
                    int destinationId = subwayPath.path().getLast();
                    destinationSubwayTimeMap
                            .computeIfAbsent(destinationId, k -> new ArrayList<>())
                            .add(subwayPath.totalTime());
                })
        );

        List<Integer> result = destinationSubwayTimeMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= MINIMUM_PEOPLE_REQUIRED)
                .sorted(Comparator.comparingDouble(entry -> calculateFairnessScore(entry.getValue())))
                .limit(MAX_SUBWAY_COUNT)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (result.isEmpty() && !destinationSubwayTimeMap.isEmpty()) {
            log.info("[검색 조건 완화] 최소 참여 인원({}) 미달 -> 전체 {}개 후보역 대상으로 중간지점 산출", MINIMUM_PEOPLE_REQUIRED, destinationSubwayTimeMap.size());

            result = destinationSubwayTimeMap.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(calculateFairnessScore(e1.getValue()), calculateFairnessScore(e2.getValue())))
                    .limit(MAX_SUBWAY_COUNT)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        if (result.isEmpty()) {
            log.warn("[후보역 없음] 지하철로 도달 가능한 중간지점을 찾을 수 없습니다. | 전체 후보역ID={}",
                    destinationSubwayTimeMap.keySet());
        } else {
            log.info("[중간지점 확정] 최종 후보역 목록 생성 완료 | 후보군: {}", result);
        }

        return result;
    }

    /**
     * 공정성 점수 계산
     * 표준편차(공정성)와 평균 소요시간(효율성)을 가중합하여 복합 점수를 반환
     * 점수가 낮을수록 좋은 중간지점
     *
     * @param times 각 출발지의 소요시간 리스트
     * @return 공정성 점수
     */
    private double calculateFairnessScore(List<Integer> times) {
        double stdDev = calculateStandardDeviation(times);
        double avg = times.stream().mapToInt(i -> i).average().orElse(0);
        return FAIRNESS_WEIGHT * stdDev + EFFICIENCY_WEIGHT * avg;
    }

    private double calculateStandardDeviation(List<Integer> times) {
        double avg = times.stream().mapToInt(i -> i).average().orElse(0);
        return Math.sqrt(times.stream()
                .mapToDouble(t -> Math.pow(t - avg, 2))
                .average()
                .orElse(0));
    }

    public Map<StartPoint, Subway> mapStartPointsToClosestSubway(List<StartPoint> startPoints) {
        return startPoints.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        startPoint -> subwayReader.readClosestSubway(startPoint.getPoint())
                ));
    }

    public Map<StartPoint, List<SubwayPathResult>> mapStartPointsToDestinationSubways(
            List<StartPoint> startPoints,
            Map<StartPoint, Subway> startPointsToClosestSubway,
            List<Subway> destinationSubways
    ) {
        return startPoints.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        startPoint -> destinationSubways.stream()
                                .map(destinationSubway ->
                                        subwayPathProcessor.findShortestPath(
                                                startPointsToClosestSubway.get(startPoint).getSubwayId(),
                                                destinationSubway.getSubwayId())
                                )
                                .filter(Objects::nonNull)
                                .toList()
                ));
    }
}
