package com.meetup.server.event.domain.value;

import java.util.UUID;

public record ParticipantKey(Long userId, UUID guestId) {
    public static ParticipantKey ofUser(Long userId) {
        return new ParticipantKey(userId, null);
    }

    public static ParticipantKey ofGuest(UUID guestId) {
        return new ParticipantKey(null, guestId);
    }
}
