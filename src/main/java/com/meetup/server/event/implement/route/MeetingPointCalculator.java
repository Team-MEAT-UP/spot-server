package com.meetup.server.event.implement.route;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.response.route.CategorizedMeetingPointResult;
import com.meetup.server.event.dto.response.route.MeetingPointResult;
import com.meetup.server.event.exception.EventErrorType;
import com.meetup.server.event.exception.EventException;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.event.implement.EventValidator;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.implement.StartPointReader;
import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.implement.processor.PopularityScorer;
import com.meetup.server.subway.implement.processor.SubwayPathResult;
import com.meetup.server.subway.implement.processor.SubwayProcessor;
import com.meetup.server.subway.implement.reader.SubwayReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingPointCalculator {

    private final EventReader eventReader;
    private final StartPointReader startPointReader;
    private final SubwayProcessor subwayProcessor;
    private final SubwayReader subwayReader;
    private final EventValidator eventValidator;
    private final PopularityScorer popularityScorer;

    public CategorizedMeetingPointResult calculate(UUID eventId) {
        Event event = eventReader.read(eventId);
        List<StartPoint> startPoints = startPointReader.readAllWithUserByEvent(event);
        eventValidator.validateMinimumStartPoints(startPoints);

        Map<StartPoint, Subway> startPointsToSubway = subwayProcessor.mapStartPointsToClosestSubway(startPoints);
        eventValidator.validateStartPointsNotAllSameSubway(startPointsToSubway);

        Point centerPoint = CoordinateUtil.calculateCenterPoint(startPoints.stream().map(StartPoint::getPoint).toList());
        List<Subway> nearbySubways = subwayReader.readNearbySubways(centerPoint);
        eventValidator.validateNearbySubwaysExist(nearbySubways);

        MeetingPointResult coordinateCandidate = calculateCoordinateCandidate(event, startPoints, startPointsToSubway, nearbySubways);
        MeetingPointResult popularityCandidate = calculatePopularityCandidate(event, startPoints, centerPoint, coordinateCandidate, nearbySubways);
        event.updateSubway(coordinateCandidate.subway());

        return CategorizedMeetingPointResult.of(coordinateCandidate, popularityCandidate);
    }

    private MeetingPointResult calculateCoordinateCandidate(Event event, List<StartPoint> startPoints, Map<StartPoint, Subway> startPointsToSubway, List<Subway> nearbySubways) {
        Map<StartPoint, List<SubwayPathResult>> subwayPaths = subwayProcessor.mapStartPointsToDestinationSubways(startPoints, startPointsToSubway, nearbySubways);
        Subway mostFairSubway = subwayProcessor.findTopFairSubway(subwayPaths)
                .orElseThrow(() -> new EventException(EventErrorType.PATH_CALCULATION_FAILED));

        return MeetingPointResult.of(event, startPoints, mostFairSubway);
    }

    private MeetingPointResult calculatePopularityCandidate(Event event, List<StartPoint> startPoints, Point centerPoint, MeetingPointResult coordinateCandidate, List<Subway> nearbySubways) {
        List<Subway> popularCandidates = subwayReader.readSubwaysForPopularity(centerPoint);
        List<Subway> topPopularCandidates = popularityScorer.scoreAndRank(popularCandidates, centerPoint);

        Subway selectedSubway = topPopularCandidates.stream()
                .filter(subway -> !coordinateCandidate.isSameSubway(subway))
                .findFirst()
                .or(() -> topPopularCandidates.stream().findFirst())
                .or(() -> nearbySubways.stream().filter(subway -> !coordinateCandidate.isSameSubway(subway)).findFirst())
                .orElse(coordinateCandidate.subway());

        return MeetingPointResult.of(event, startPoints, selectedSubway);
    }
}
