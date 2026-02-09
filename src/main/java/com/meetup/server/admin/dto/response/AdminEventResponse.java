package com.meetup.server.admin.dto.response;

import com.meetup.server.event.domain.Event;
import com.meetup.server.place.domain.Place;
import com.meetup.server.subway.domain.Subway;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record AdminEventResponse(
        UUID eventId,
        String eventName,
        LocalDateTime eventDateTime,
        String meetingPoint,
        String meetingPlace,
        int participantCount,
        int kakaoInflowCount,
        LocalDateTime createdDateTime
) {
    public static AdminEventResponse of(Event event, int participantCount, int kakaoInflowCount) {
        return new AdminEventResponse(
                event.getEventId(),
                event.getEventName(),
                event.getEventDateTime(),
                Optional.ofNullable(event.getSubway()).map(Subway::getName).orElse(null),
                Optional.ofNullable(event.getPlace()).map(Place::getName).orElse(null),
                participantCount,
                kakaoInflowCount,
                event.getCreatedAt()
        );
    }
}
