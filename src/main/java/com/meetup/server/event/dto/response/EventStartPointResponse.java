package com.meetup.server.event.dto.response;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.util.UsernameExtractor;
import com.meetup.server.startpoint.domain.StartPoint;
import lombok.Builder;

import java.util.UUID;

@Builder
public record EventStartPointResponse(
        UUID eventId,
        UUID startPointId,
        UUID guestId,
        String username
) {
    public static EventStartPointResponse of(Event event, StartPoint startPoint) {
        return EventStartPointResponse.builder()
                .eventId(event.getEventId())
                .startPointId(startPoint.getStartPointId())
                .guestId(startPoint.getGuestId())
                .username(UsernameExtractor.extractDisplayName(startPoint))
                .build();
    }
}
