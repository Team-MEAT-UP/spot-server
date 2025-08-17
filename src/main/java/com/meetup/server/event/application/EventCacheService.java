package com.meetup.server.event.application;

import com.meetup.server.event.dto.response.route.MeetingPointResult;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.route.MeetingPointRoutesResponse;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.event.implement.route.MeetingPointCalculator;
import com.meetup.server.event.implement.route.RouteAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventCacheService {

    private final CacheManager cacheManager;

    private final MeetingPointCalculator meetingPointCalculator;
    private final RouteAssembler routeAssembler;
    private final EventReader eventReader;


    @Cacheable(value = "routeDetails", key = "#eventId", unless = "#result == null")
    public MeetingPointRoutesResponse getCachedMeetingPointRoutes(UUID eventId) {
        List<MeetingPointResult> meetingPointResults = meetingPointCalculator.calculate(eventId);
        List<MeetingPointRouteGroup> meetingPointRouteGroups = meetingPointResults.stream()
                .map(resultResponse -> routeAssembler.assemble(resultResponse.startPoints(), resultResponse.subway()))
                .toList();
        return MeetingPointRoutesResponse.of(meetingPointResults, meetingPointRouteGroups);
    }

    public void updateCachedPlaceName(UUID eventId, String placeName) {
        MeetingPointRoutesResponse cachedData = eventReader.readEventCache(eventId);
        if (cachedData == null) {
            return;
        }

        Cache cache = cacheManager.getCache("routeDetails");
        cache.put(eventId, cachedData.withPlaceName(placeName));
    }
}
