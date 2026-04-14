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
    private static final double FAIRNESS_WEIGHT = 0.6;
    private static final double EFFICIENCY_WEIGHT = 0.4;

    private final SubwayReader subwayReader;
    private final SubwayPathProcessor subwayPathProcessor;

    public Optional<Subway> findTopFairSubway(Map<StartPoint, List<SubwayPathResult>> subwayPaths) {
        Map<Integer, List<Integer>> destinationSubwayTimes = new HashMap<>();

        subwayPaths.forEach((startPoint, paths) ->
                paths.forEach(subwayPath -> {
                    int destinationId = subwayPath.path().getLast();
                    destinationSubwayTimes
                            .computeIfAbsent(destinationId, k -> new ArrayList<>())
                            .add(subwayPath.totalTime());
                })
        );

        if (destinationSubwayTimes.isEmpty()) {
            return Optional.empty();
        }

        return findOptimalSubwayId(destinationSubwayTimes)
                .map(subwayReader::read);
    }

    private Optional<Integer> findOptimalSubwayId(Map<Integer, List<Integer>> destinationSubwayTimes) {
        Optional<Integer> bestSubwayId = destinationSubwayTimes.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= MINIMUM_PEOPLE_REQUIRED)
                .min(Comparator.comparingDouble(entry -> calculateFairnessScore(entry.getValue())))
                .map(Map.Entry::getKey);

        if (bestSubwayId.isPresent()) {
            return bestSubwayId;
        }

        return destinationSubwayTimes.entrySet().stream()
                .min(Comparator.comparingDouble(entry -> calculateFairnessScore(entry.getValue())))
                .map(Map.Entry::getKey);
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
            Map<StartPoint, Subway> closestSubways,
            List<Subway> destinationSubways
    ) {
        return startPoints.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        startPoint -> destinationSubways.stream()
                                .map(destinationSubway ->
                                        subwayPathProcessor.findShortestPath(
                                                closestSubways.get(startPoint).getSubwayId(),
                                                destinationSubway.getSubwayId())
                                )
                                .filter(Objects::nonNull)
                                .toList()
                ));
    }
}
