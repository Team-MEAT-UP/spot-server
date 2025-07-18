package com.meetup.server.event.application;

import com.meetup.server.event.dto.response.MeetingPointResult;
import com.meetup.server.event.dto.response.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.MeetingPointRoutesResponse;
import com.meetup.server.event.implement.MeetingPointCalculator;
import com.meetup.server.event.implement.RouteAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventCacheService {

    private final MeetingPointCalculator meetingPointCalculator;
    private final RouteAssembler routeAssembler;

    @Cacheable(value = "routeDetails", key = "#eventId", unless = "#result == null")
    public MeetingPointRoutesResponse getCachedMeetingPointRoutes(UUID eventId) {
        List<MeetingPointResult> meetingPointResults = meetingPointCalculator.calculate(eventId);
        List<MeetingPointRouteGroup> meetingPointRouteGroups = meetingPointResults.stream()
                .map(resultResponse -> routeAssembler.assemble(resultResponse.startPoints(), resultResponse.subway()))
                .toList();
        return MeetingPointRoutesResponse.of(meetingPointResults, meetingPointRouteGroups);
    }
}
