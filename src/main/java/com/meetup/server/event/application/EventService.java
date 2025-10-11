package com.meetup.server.event.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.request.EventRequest;
import com.meetup.server.event.dto.request.UpdateEventRequest;
import com.meetup.server.event.dto.request.UpdatePlaceRequest;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.event.dto.response.route.MeetingPointResult;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.route.MeetingPointRoutesResponse;
import com.meetup.server.event.implement.EventLockManager;
import com.meetup.server.event.implement.EventProcessor;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.event.implement.route.MeetingPointCalculator;
import com.meetup.server.event.implement.route.RouteProcessor;
import com.meetup.server.event.implement.route.RouteReader;
import com.meetup.server.global.support.Performance;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.implement.PlaceReader;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.implement.StartPointProcessor;
import com.meetup.server.startpoint.implement.StartPointReader;
import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.implement.reader.SubwayReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventReader eventReader;
    private final EventProcessor eventProcessor;
    private final StartPointProcessor startPointProcessor;
    private final MeetingPointCalculator meetingPointCalculator;
    private final RouteReader routeReader;
    private final RouteProcessor routeProcessor;
    private final StartPointReader startPointReader;
    private final PlaceReader placeReader;
    private final SubwayReader subwayReader;
    private final EventLockManager eventLockManager;

    public EventStartPointResponse createEvent(Long userId, UUID guestId, EventRequest eventRequest) {
        Event event = eventProcessor.save(eventRequest);
        StartPoint startPoint = startPointProcessor.save(event, userId, guestId, eventRequest.toStartPointRequest());
        return EventStartPointResponse.of(event, startPoint);
    }

    @Performance
    public MeetingPointRoutesResponse getMeetingPointRoutes(UUID eventId, Long userId, UUID guestId) {
        List<MeetingPointRouteGroup> meetingPointRouteGroups = getRouteGroups(eventId);

        Event event = eventReader.read(eventId);
        List<StartPoint> startPoints = startPointReader.readAll(event);

        for (MeetingPointRouteGroup group : meetingPointRouteGroups) {
            routeProcessor.prioritizeMyRoute(userId, guestId, group.routeResponse());
        }

        return MeetingPointRoutesResponse.of(event, startPoints, meetingPointRouteGroups);
    }

    private List<MeetingPointRouteGroup> getRouteGroups(UUID eventId) {
        List<MeetingPointRouteGroup> meetingPointRouteGroupsCache = routeReader.readRouteGroups(eventId);

        if (!meetingPointRouteGroupsCache.isEmpty()) {
            return meetingPointRouteGroupsCache;
        }

        return calculateAndSaveRouteGroups(eventId);
    }

    private List<MeetingPointRouteGroup> calculateAndSaveRouteGroups(UUID eventId) {
        ReentrantLock reentrantLock = eventLockManager.getLock(eventId);

        reentrantLock.lock();
        try {
            List<MeetingPointRouteGroup> meetingPointRouteGroupsCache = routeReader.readRouteGroups(eventId);

            if (!meetingPointRouteGroupsCache.isEmpty()) {
                return meetingPointRouteGroupsCache;
            }

            List<MeetingPointResult> meetingPointResults = meetingPointCalculator.calculate(eventId);
            List<MeetingPointRouteGroup> meetingPointRouteGroups = routeProcessor.buildRouteGroups(meetingPointResults);
            routeProcessor.saveRouteGroups(eventId, meetingPointRouteGroups);

            return meetingPointRouteGroups;
        } finally {
            reentrantLock.unlock();
        }
    }

    public void updateEvent(UUID eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventReader.read(eventId);
        eventProcessor.update(event, updateEventRequest);
    }

    public void deleteEvent(UUID eventId) {
        Event event = eventReader.read(eventId);

        routeProcessor.deleteCache(eventId);
        eventProcessor.delete(event);
    }

    public void updatePlace(UUID eventId, UpdatePlaceRequest updatePlaceRequest) {
        Event event = eventReader.read(eventId);
        Place place = placeReader.read(updatePlaceRequest.placeId());
        Subway subway = subwayReader.read(updatePlaceRequest.subwayId());

        event.updateMeetingPlace(place, subway);
    }

    public void deletePlace(UUID eventId) {
        Event event = eventReader.read(eventId);
        event.deletePlace();
    }
}
