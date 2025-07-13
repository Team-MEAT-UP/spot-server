package com.meetup.server.startpoint.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.global.clients.kakao.local.KakaoLocalResponse;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.dto.request.StartPointRequest;
import com.meetup.server.startpoint.exception.InvalidStartPointException;
import com.meetup.server.startpoint.exception.StartPointErrorType;
import com.meetup.server.startpoint.implement.StartPointProcessor;
import com.meetup.server.startpoint.implement.StartPointReader;
import com.meetup.server.startpoint.implement.StartPointSearcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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

    @Transactional
    @CacheEvict(value = "routeDetails", key = "#eventId")
    public EventStartPointResponse upsertStartPoint(UUID eventId, Long userId, UUID guestId, UUID startPointId, StartPointRequest startPointRequest) {
        Event event = eventReader.read(eventId);

        if (startPointId != null) {
            return updateStartPoint(startPointId, startPointRequest);
        }
        return createStartPoint(event, userId, guestId, startPointRequest);
    }

    @Transactional
    @CacheEvict(value = "routeDetails", key = "#eventId")
    public void deleteStartPoint(UUID eventId, UUID startPointId) {
        StartPoint startPoint = startPointReader.read(startPointId);

        if (!startPoint.getEvent().getEventId().equals(eventId)) {
            throw new InvalidStartPointException(StartPointErrorType.INVALID_START_POINT);
        }

        startPointProcessor.delete(startPoint);
    }

    public KakaoLocalResponse searchStartPoint(String textQuery) {
        return startPointSearcher.search(textQuery);
    }

    private EventStartPointResponse updateStartPoint(UUID startPointId, StartPointRequest startPointRequest) {
        StartPoint startPoint = startPointReader.read(startPointId);
        startPointProcessor.update(startPoint, startPointRequest);
        return EventStartPointResponse.of(startPoint.getEvent(), startPoint);
    }

    private EventStartPointResponse createStartPoint(Event event, Long userId, UUID guestId, StartPointRequest startPointRequest) {
        List<StartPoint> startPointList = startPointReader.readAll(event);
        boolean alreadyHas = userId != null && validateAlreadyHasStartPoint(userId, startPointList);
        StartPoint startPoint;

        if (alreadyHas) {
            startPoint = startPointProcessor.saveByGuest(
                    event,
                    null,
                    startPointRequest
            );
        } else {
            startPoint = startPointProcessor.save(event, userId, guestId, startPointRequest);
        }
        return EventStartPointResponse.of(event, startPoint);
    }

    private boolean validateAlreadyHasStartPoint(Long userId, List<StartPoint> startPointList) {
        return startPointList
                .stream()
                .filter(StartPoint::getIsUser)
                .anyMatch(startPoint -> startPoint.getUser().getUserId().equals(userId));
    }
}
