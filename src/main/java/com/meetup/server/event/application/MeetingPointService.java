package com.meetup.server.event.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.response.MeetingPointResult;
import com.meetup.server.event.exception.EventErrorType;
import com.meetup.server.event.exception.EventException;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.event.implement.EventValidator;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.implement.StartPointReader;
import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.implement.processor.SubwayPathResult;
import com.meetup.server.subway.implement.processor.SubwayProcessor;
import com.meetup.server.subway.implement.reader.SubwayReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingPointService {

    private final EventReader eventReader;
    private final StartPointReader startPointReader;
    private final SubwayProcessor subwayProcessor;
    private final SubwayReader subwayReader;
    private final EventValidator eventValidator;

    @Transactional
    public List<MeetingPointResult> getMeetingPoints(UUID eventId) {
        Event event = eventReader.read(eventId);
        List<StartPoint> startPoints = startPointReader.readAll(event);
        eventValidator.validateMinimumStartPoints(startPoints);

        Map<StartPoint, Subway> startPointToSubwayMap = subwayProcessor.mapStartPointsToClosestSubway(startPoints);
        eventValidator.validateStartPointsNotAllSameSubway(startPointToSubwayMap);

        Point centerPoint = CoordinateUtil.calculateCenterPoint(startPoints.stream().map(StartPoint::getPoint).toList());
        List<Subway> nearbySubways = subwayReader.readNearbySubways(centerPoint);
        eventValidator.validateNearbySubwaysExist(nearbySubways);

        Map<StartPoint, List<SubwayPathResult>> startPointToSubwayPathsMap = subwayProcessor.mapStartPointsToDestinationSubways(startPoints, startPointToSubwayMap, nearbySubways);

        List<Integer> topFairSubwayIds = subwayProcessor.findTopFairSubways(startPointToSubwayPathsMap);
        if (topFairSubwayIds.isEmpty()) {
            throw new EventException(EventErrorType.PATH_CALCULATION_FAILED);
        }

        return subwayReader.readByIdIn(topFairSubwayIds).stream()
                .map(subway -> MeetingPointResult.of(event, startPoints, subway))
                .toList();
    }
}
