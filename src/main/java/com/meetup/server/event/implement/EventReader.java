package com.meetup.server.event.implement;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.exception.EventErrorType;
import com.meetup.server.event.exception.EventException;
import com.meetup.server.event.infrastructure.jpa.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventReader {

    private final EventRepository eventRepository;

    public Event read(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorType.EVENT_NOT_FOUND));
    }

    public List<Event> readEventsAtWithPlace(int hour) {
        LocalDateTime targetTime = LocalDateTime.now()
                .plusHours(hour)
                .withSecond(0)
                .withNano(0);
        return eventRepository.findAllByEventDateTimeWithPlace(targetTime);
    }
}
