package com.meetup.server.subway.implement.processor;

import java.util.List;

public record SubwayPathNode(
        int subwayId,
        int totalTime,
        List<Integer> subwayIds
) {
}
