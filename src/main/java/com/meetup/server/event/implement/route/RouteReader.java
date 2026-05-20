package com.meetup.server.event.implement.route;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.event.infrastructure.redis.CachedRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteReader {

    private final EventReader eventReader;
    private final CachedRouteRepository cachedRouteRepository;

    public MeetingPointRouteGroups readMeetingPointRoutes(UUID eventId) {
        return readByEventId(eventId)
                .orElse(MeetingPointRouteGroups.of(null, null));
    }

    public Optional<MeetingPointRouteGroups> readByEventId(UUID eventId) {
        return Optional.ofNullable(cachedRouteRepository.findByEventId(eventId))
                .or(() -> fallback(eventId));
    }

    private Optional<MeetingPointRouteGroups> fallback(UUID eventId) {
        Event event = eventReader.read(eventId);

        if (event.getRoutes() == null) {
            return Optional.empty();
        }

        MeetingPointRouteGroups routes = MeetingPointRouteGroups.of(
                event.getRoutes().byCoordinate(),
                event.getRoutes().byPopularity()
        );
        cachedRouteRepository.save(eventId, routes);

        return Optional.of(routes);
    }
}
