package com.meetup.server.startpoint.infrastructure.querydsl.projection;

import java.util.UUID;

public record ParticipantCount(
        UUID eventId,
        Long count
) {}
