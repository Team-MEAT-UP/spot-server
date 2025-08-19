package com.meetup.server.event.implement;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.exception.EventErrorType;
import com.meetup.server.event.exception.EventException;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.infrastructure.jpa.StartPointRepository;
import com.meetup.server.subway.domain.Subway;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EventValidator {

    private static final int MIN_START_POINTS = 2;
    private static final int MAX_START_POINTS = 8;

    private final StartPointRepository startPointRepository;

    public void validateEventIsNotFull(Event event) {
        if (startPointRepository.countByEvent(event) >= MAX_START_POINTS) {
            throw new EventException(EventErrorType.START_POINT_LIMIT_EXCEEDED);
        }
    }

    public void validateMinimumStartPoints(List<StartPoint> startPoints) {
        if (startPoints.size() < MIN_START_POINTS) {
            throw new EventException(EventErrorType.INSUFFICIENT_START_POINTS);
        }
    }

    public void validateStartPointsNotAllSameSubway(Map<StartPoint, Subway> startPointToSubwayMap) {
        Set<Subway> uniqueSubways = new HashSet<>(startPointToSubwayMap.values());
        if (uniqueSubways.size() == 1) {
            throw new EventException(EventErrorType.SAME_SUBWAY_FOR_ALL_START_POINTS);
        }
    }

    public void validateNearbySubwaysExist(List<Subway> nearBySubways) {
        if (nearBySubways.isEmpty()) {
            throw new EventException(EventErrorType.NO_INTERMEDIATE_SUBWAYS_FOUND);
        }
    }

    public void validateEventCacheExist(Cache.ValueWrapper eventCache) {
        if (eventCache == null) {
            throw new EventException(EventErrorType.CACHE_NOT_FOUND);
        }
    }
}
