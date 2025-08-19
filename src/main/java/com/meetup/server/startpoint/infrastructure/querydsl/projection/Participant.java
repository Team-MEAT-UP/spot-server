package com.meetup.server.startpoint.infrastructure.querydsl.projection;

import java.util.UUID;

public record Participant(
        UUID eventId,
        String profileImageUrl
) {
}
