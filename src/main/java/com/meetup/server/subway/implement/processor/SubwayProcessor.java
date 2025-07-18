package com.meetup.server.subway.implement.processor;

import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.implement.reader.SubwayReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubwayProcessor {

    private static final int MINIMUM_PEOPLE_REQUIRED = 2;
    private static final int MAX_SUBWAY_COUNT = 3;

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

        return destinationSubwayTimeMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= MINIMUM_PEOPLE_REQUIRED)
                .sorted(Comparator.comparingDouble(entry -> calculateStandardDeviation(entry.getValue())))
                .limit(MAX_SUBWAY_COUNT)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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
