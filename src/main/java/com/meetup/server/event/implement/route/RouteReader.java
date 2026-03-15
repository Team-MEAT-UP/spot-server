package com.meetup.server.event.implement.route;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.RouteCache;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.implement.EventReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteReader {

    private final EventReader eventReader;
    private final RouteCache routeCache;

    public List<MeetingPointRouteGroup> readRouteGroups(UUID eventId) {
        return readByEventId(eventId)
                .map(MeetingPointRouteGroups::meetingPointRouteGroups)
                .orElse(Collections.emptyList());
    }

    public Optional<MeetingPointRouteGroups> readByEventId(UUID eventId) {
        return routeCache.getByEventId(eventId)
                .or(() -> fallback(eventId));
    }

    private Optional<MeetingPointRouteGroups> fallback(UUID eventId) {
        Event event = eventReader.read(eventId);

        if (event.getRoutes() == null) {
            return Optional.empty();
        }

        MeetingPointRouteGroups meetingPointRouteGroups = MeetingPointRouteGroups.from(event.getRoutes().meetingPointRouteGroups());
        routeCache.save(eventId, meetingPointRouteGroups);

        return Optional.of(meetingPointRouteGroups);
    }
}
