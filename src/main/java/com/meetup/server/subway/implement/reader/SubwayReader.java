package com.meetup.server.subway.implement.reader;

import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.exception.SubwayErrorType;
import com.meetup.server.subway.exception.SubwayException;
import com.meetup.server.subway.infrastructure.jpa.SubwayRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubwayReader {

    private static final double NEARBY_SUBWAY_RADIUS_M = 1500;
    private static final double POPULARITY_RADIUS_M = 4000;
    private static final double MAX_SEARCH_RADIUS_M = 10000;
    private static final double RADIUS_INCREMENT_M = 500;
    private static final int MIN_NEARBY_COUNT = 5;
    private static final int MIN_POPULARITY_COUNT = 15;

    private final SubwayRepository subwayRepository;

    public Subway read(int subwayId) {
        return subwayRepository.findById(subwayId).orElseThrow(() -> new SubwayException(SubwayErrorType.SUBWAY_NOT_FOUND));
    }

    public Subway readClosestSubway(Point startPoint) {
        return subwayRepository.findClosestSubway(startPoint);
    }

    public List<Subway> readAllWithinRadius(Point centerPoint, double radius) {
        return subwayRepository.findAllWithinRadius(centerPoint, radius);
    }

    public List<Subway> readNearbySubways(Point centerPoint) {
        return expandUntilMinCount(centerPoint, NEARBY_SUBWAY_RADIUS_M, MIN_NEARBY_COUNT);
    }

    public List<Subway> readSubwaysForPopularity(Point centerPoint) {
        return expandUntilMinCount(centerPoint, POPULARITY_RADIUS_M, MIN_POPULARITY_COUNT);
    }

    private List<Subway> expandUntilMinCount(Point centerPoint, double initialRadius, int minCount) {
        double radius = initialRadius;
        List<Subway> subways = new ArrayList<>();
        while (radius <= MAX_SEARCH_RADIUS_M) {
            subways = readAllWithinRadius(centerPoint, radius);
            if (subways.size() >= minCount) break;
            radius += RADIUS_INCREMENT_M;
        }
        return subways;
    }
}
