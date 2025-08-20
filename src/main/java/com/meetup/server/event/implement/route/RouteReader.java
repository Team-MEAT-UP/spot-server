package com.meetup.server.event.implement.route;

import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.infrastructure.redis.CachedRouteRepository;
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
                .map(MeetingPointRouteGroups::meetingPointRouteGroups)
                .orElse(Collections.emptyList());
    }
}
