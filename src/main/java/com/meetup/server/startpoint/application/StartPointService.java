package com.meetup.server.startpoint.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.event.implement.EventProcessor;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.event.implement.route.RouteProcessor;
import com.meetup.server.global.clients.kakao.local.KakaoLocalResponse;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.dto.request.StartPointRequest;
import com.meetup.server.startpoint.implement.StartPointProcessor;
import com.meetup.server.startpoint.implement.StartPointReader;
import com.meetup.server.startpoint.implement.StartPointSearcher;
import com.meetup.server.startpoint.implement.StartPointValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartPointService {

    private final StartPointReader startPointReader;
    private final StartPointProcessor startPointProcessor;
    private final StartPointSearcher startPointSearcher;
    private final EventReader eventReader;
    private final StartPointValidator startPointValidator;
    private final RouteProcessor routeProcessor;
    private final EventProcessor eventProcessor;

    @Transactional
    public EventStartPointResponse createStartPoint(UUID eventId, Long userId, UUID guestId, StartPointRequest startPointRequest) {
        Event event = eventReader.read(eventId);
        List<StartPoint> startPointList = startPointReader.readAll(event);

        if (userId != null && validateAlreadyHasStartPoint(userId, startPointList)) {
            StartPoint startPoint = startPointProcessor.saveByGuest(
                    event,
                    null,
                    startPointRequest
            );
            return EventStartPointResponse.of(event, startPoint);
        }

        StartPoint startPoint = startPointProcessor.save(event, userId, guestId, startPointRequest);

        routeProcessor.deleteCache(eventId);
        eventProcessor.deleteRoute(eventId);

        return EventStartPointResponse.of(event, startPoint);
    }

    @Transactional
    public EventStartPointResponse updateStartPoint(UUID eventId, UUID startPointId, StartPointRequest startPointRequest) {
        StartPoint startPoint = startPointReader.read(startPointId);

        startPointValidator.validateBelongsToEvent(eventId, startPoint);
        startPointProcessor.update(startPoint, startPointRequest);

        routeProcessor.deleteCache(eventId);
        eventProcessor.deleteRoute(eventId);

        return EventStartPointResponse.of(startPoint.getEvent(), startPoint);
    }

    @Transactional
    public void deleteStartPoint(UUID eventId, UUID startPointId) {
        StartPoint startPoint = startPointReader.read(startPointId);
        startPointValidator.validateBelongsToEvent(eventId, startPoint);
        startPointProcessor.delete(startPoint);

        routeProcessor.deleteCache(eventId);
        eventProcessor.deleteRoute(eventId);
    }

    public KakaoLocalResponse searchStartPoint(String textQuery) {
        return startPointSearcher.search(textQuery);
    }

    private boolean validateAlreadyHasStartPoint(Long userId, List<StartPoint> startPointList) {
        return startPointList
                .stream()
                .filter(StartPoint::getIsUser)
                .anyMatch(startPoint -> startPoint.getUser().getUserId().equals(userId));
    }
}
