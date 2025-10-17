package com.meetup.server.event.domain.listener;

import java.util.UUID;

public record StartPointChangedEvent(
        UUID eventId
) {
    public static StartPointChangedEvent from(UUID eventId) {
        return new StartPointChangedEvent(eventId);
    }
}
