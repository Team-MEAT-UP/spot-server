package com.meetup.server.event.domain;

import com.meetup.server.event.domain.value.MeetingPointRouteGroups;

import java.util.Optional;
import java.util.UUID;

public interface RouteCache {
    Optional<MeetingPointRouteGroups> getByEventId(UUID eventId);
    void save(UUID eventId, MeetingPointRouteGroups cached);
    void delete(UUID eventId);
}
