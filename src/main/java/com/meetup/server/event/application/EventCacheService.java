package com.meetup.server.event.application;

import com.meetup.server.event.dto.response.MeetingPointResult;
import com.meetup.server.event.dto.response.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.MeetingPointRoutesResponse;
import com.meetup.server.event.implement.EventProcessor;
import com.meetup.server.event.implement.EventReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventCacheService {

    private final MeetingPointService meetingPointService;
    private final RouteService routeService;
    private final EventReader eventReader;
    private final EventProcessor eventProcessor;

    @Cacheable(value = "routeDetails", key = "#eventId", unless = "#result == null")
    public MeetingPointRoutesResponse getCachedMeetingPointRoutes(UUID eventId) {
        List<MeetingPointResult> resultResponses = meetingPointService.getMeetingPoints(eventId);
        List<MeetingPointRouteGroup> meetingPointRouteGroups = resultResponses.stream()
                .map(resultResponse -> routeService.getAllRouteDetails(resultResponse.event(), resultResponse.startPoints(), resultResponse.subway()))
                .toList();
        return new MeetingPointRoutesResponse(meetingPointRouteGroups);
    }

    @Transactional
    @CachePut(value = "routeDetails", key = "#eventId")
    public MeetingPointRoutesResponse updateTransit(UUID eventId, UUID startPointId, boolean isTransit) {
        MeetingPointRoutesResponse meetingPointRoutes = eventReader.readEventCache(eventId);
        eventProcessor.updateTransitForStartPoint(meetingPointRoutes.meetingPointRouteGroups(), startPointId, isTransit);
        return meetingPointRoutes;
    }
}
