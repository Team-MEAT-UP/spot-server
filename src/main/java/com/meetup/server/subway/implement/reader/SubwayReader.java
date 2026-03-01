package com.meetup.server.subway.implement.reader;

import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.exception.SubwayErrorType;
import com.meetup.server.subway.exception.SubwayException;
import com.meetup.server.subway.infrastructure.jpa.SubwayRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubwayReader {

    private static final double NEAREST_SUBWAY_RADIUS_M = 1500;
    private static final double MAX_SEARCH_RADIUS_M = 10000;
    private static final double RADIUS_INCREMENT_M = 500;
    private static final int MIN_CANDIDATE_COUNT = 5;

    private final SubwayRepository subwayRepository;

    public Subway read(int subwayId) {
        return subwayRepository.findById(subwayId).orElseThrow(() -> new SubwayException(SubwayErrorType.SUBWAY_NOT_FOUND));
    }

    public List<Subway> readAllOrderByIds(List<Integer> subwayIds) {
        return subwayRepository.findAllBySubwayIdInOrderByIds(subwayIds.toArray(new Integer[0]));
    }

    public Subway readClosestSubway(Point startPoint) {
        return subwayRepository.findClosestSubway(startPoint);
    }

    public List<Subway> readAllWithinRadius(Point centerPoint, double radius) {
        return subwayRepository.findAllWithinRadius(centerPoint, radius);
    }

    public List<Subway> readNearbySubways(Point centerPoint) {
        double radius = NEAREST_SUBWAY_RADIUS_M;

        List<Subway> nearbySubways = new ArrayList<>();

        while (radius <= MAX_SEARCH_RADIUS_M) {
            nearbySubways = readAllWithinRadius(centerPoint, radius);

            if (nearbySubways.size() >= MIN_CANDIDATE_COUNT) {
                break;
            }

            radius += RADIUS_INCREMENT_M;
        }

        return nearbySubways;
    }
}
