package com.meetup.server.log.infrastructure.jpa.projection;

import java.util.UUID;

public record EventInflowCount(
        UUID eventId,
        Long inflowCount
) {
}
