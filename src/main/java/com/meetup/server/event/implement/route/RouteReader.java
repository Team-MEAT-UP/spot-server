package com.meetup.server.event.implement.route;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.exception.EventErrorType;
import com.meetup.server.event.exception.EventException;
import com.meetup.server.event.infrastructure.jpa.EventRepository;
import com.meetup.server.event.infrastructure.redis.CachedRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteReader {

    private final EventRepository eventRepository;
    private final CachedRouteRepository cachedRouteRepository;

    public List<MeetingPointRouteGroup> readRouteGroups(UUID eventId) {
        return findByEventId(eventId)
                .map(MeetingPointRouteGroups::meetingPointRouteGroups)
                .orElse(Collections.emptyList());
    }

    public Optional<MeetingPointRouteGroups> findByEventId(UUID eventId) {
        return Optional.ofNullable(cachedRouteRepository.getFromCache(eventId))
                .or(() -> fallback(eventId));
    }

    private Optional<MeetingPointRouteGroups> fallback(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorType.EVENT_NOT_FOUND));

        if (event.getRoutes() == null) {
            return Optional.empty();
        }

        MeetingPointRouteGroups meetingPointRouteGroups = MeetingPointRouteGroups.from(event.getRoutes().meetingPointRouteGroups());
        cachedRouteRepository.save(eventId, meetingPointRouteGroups);

        return Optional.of(meetingPointRouteGroups);
    }
}
