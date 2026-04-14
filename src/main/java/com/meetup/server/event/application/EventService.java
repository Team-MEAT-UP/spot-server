package com.meetup.server.event.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.request.EventRequest;
import com.meetup.server.event.dto.request.UpdateEventRequest;
import com.meetup.server.event.dto.request.UpdatePlaceRequest;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.event.dto.response.route.CategorizedMeetingPointResult;
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
import java.util.stream.Stream;

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
        MeetingPointRouteGroups meetingPointRoutes = getRouteGroups(eventId);

        Event event = eventReader.read(eventId);
        List<StartPoint> startPoints = startPointReader.readAllWithUserByEvent(event);

        Stream.of(meetingPointRoutes.byCoordinate(), meetingPointRoutes.byPopularity())
                .forEach(group -> prioritizeRouteGroup(group, userId, guestId));

        return MeetingPointRoutesResponse.of(event, startPoints, meetingPointRoutes);
    }

    private MeetingPointRouteGroups getRouteGroups(UUID eventId) {
        MeetingPointRouteGroups cachedRoutes = routeReader.readMeetingPointRoutes(eventId);

        if (isCacheValid(cachedRoutes)) {
            return cachedRoutes;
        }

        return calculateAndSaveRouteGroups(eventId);
    }

    private MeetingPointRouteGroups calculateAndSaveRouteGroups(UUID eventId) {
        ReentrantLock lock = eventLockManager.getLock(eventId);
        lock.lock();
        try {
            MeetingPointRouteGroups cachedRoutes = routeReader.readMeetingPointRoutes(eventId);
            if (isCacheValid(cachedRoutes)) {
                return cachedRoutes;
            }

            CategorizedMeetingPointResult result = meetingPointCalculator.calculate(eventId);
            MeetingPointRouteGroup coordinateRoute = routeProcessor.buildRouteGroup(result.byCoordinate());
            MeetingPointRouteGroup popularRoute = routeProcessor.buildRouteGroup(result.byPopularity());

            MeetingPointRouteGroups calculatedRoutes = MeetingPointRouteGroups.of(coordinateRoute, popularRoute);
            routeProcessor.saveRouteGroups(eventId, calculatedRoutes);

            return calculatedRoutes;
        } finally {
            lock.unlock();
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

    private void prioritizeRouteGroup(MeetingPointRouteGroup group, Long userId, UUID guestId) {
        if (group != null) {
            routeProcessor.prioritizeMyRoute(userId, guestId, group.routeResponse());
        }
    }

    private boolean isCacheValid(MeetingPointRouteGroups groups) {
        return groups != null && groups.byCoordinate() != null && groups.byPopularity() != null;
    }
}
