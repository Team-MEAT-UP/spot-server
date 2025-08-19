package com.meetup.server.event.implement.route;

import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.infrastructure.redis.CachedRouteRepository;
import com.meetup.server.event.infrastructure.redis.MeetingPointRouteGroupsCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteReader {

    private final CachedRouteRepository cachedRouteRepository;

    public List<MeetingPointRouteGroup> readRouteGroups(UUID eventId) {
        return cachedRouteRepository.findByEventId(eventId)
                .map(MeetingPointRouteGroupsCache::meetingPointRouteGroups)
                .orElse(Collections.emptyList());
    }
}
