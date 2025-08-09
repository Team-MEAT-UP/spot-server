package com.meetup.server.event.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.request.EventRequest;
import com.meetup.server.event.dto.request.UpdateEventRequest;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.event.dto.response.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.MeetingPointRoutesResponse;
import com.meetup.server.event.implement.EventProcessor;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.implement.StartPointProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventReader eventReader;
    private final EventProcessor eventProcessor;
    private final StartPointProcessor startPointProcessor;
    private final EventCacheService eventCacheService;

    public EventStartPointResponse createEvent(Long userId, UUID guestId, EventRequest eventRequest) {
        Event event = eventProcessor.save(eventRequest);
        StartPoint startPoint = startPointProcessor.save(event, userId, guestId, eventRequest.toStartPointRequest());
        return EventStartPointResponse.of(event, startPoint);
    }

    public MeetingPointRoutesResponse getMeetingPointRoutes(UUID eventId, Long userId, UUID guestId) {
        MeetingPointRoutesResponse meetingPointRoutesResponse = eventCacheService.getCachedMeetingPointRoutes(eventId);
        for (MeetingPointRouteGroup event : meetingPointRoutesResponse.meetingPointRouteGroups()) {
            eventProcessor.prioritizeMyRoute(userId, guestId, event.getRouteResponse());
        }
        return meetingPointRoutesResponse;
    }

    @CachePut(value = "routeDetails", key = "#eventId")
    public MeetingPointRoutesResponse updateEvent(UUID eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventReader.read(eventId);
        eventProcessor.update(event, updateEventRequest);

        MeetingPointRoutesResponse cachedResponse = eventReader.readEventCache(eventId);
        return cachedResponse.withEvent(updateEventRequest.eventName(), updateEventRequest.toDateTime());
    }

    @CacheEvict(value = "routeDetails", key = "#eventId")
    public void deleteEvent(UUID eventId) {
        Event event = eventReader.read(eventId);
        eventProcessor.delete(event);
    }
}
