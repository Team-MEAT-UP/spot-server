package com.meetup.server.subway.implement.reader;

import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.persistence.SubwayRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubwayReader {

    private static final double NEAREST_SUBWAY_RADIUS_M = 1500;
    private static final double MAX_SEARCH_RADIUS_M = 5000;
    private static final double RADIUS_INCREMENT_M = 500;

    private final SubwayRepository subwayRepository;

    public Subway read(int subwayId) {
        return subwayRepository.findById(subwayId).orElseThrow();
    }

    public List<Subway> readByIdIn(List<Integer> subwayIds) {
        return subwayRepository.findBySubwayIdIn(subwayIds);
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

            if (!nearbySubways.isEmpty()) {
                break;
            }

            radius += RADIUS_INCREMENT_M;
        }

        return nearbySubways;
    }
}
