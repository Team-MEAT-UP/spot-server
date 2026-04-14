package com.meetup.server.subway.implement.processor;

import java.util.List;

public record SubwayPathResult(
        int totalTime,
        List<Integer> path,
        List<String> stationNames
) {
}
