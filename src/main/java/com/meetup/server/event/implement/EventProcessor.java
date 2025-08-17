package com.meetup.server.event.implement;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.request.EventRequest;
import com.meetup.server.event.dto.request.UpdateEventRequest;
import com.meetup.server.event.infrastructure.jpa.EventRepository;
import com.meetup.server.review.implement.ReviewWriter;
import com.meetup.server.startpoint.implement.StartPointProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventProcessor {

    private final EventRepository eventRepository;
    private final StartPointProcessor startPointProcessor;
    private final ReviewWriter reviewWriter;

    public Event save(EventRequest eventRequest) {
        Event event = Event.builder()
                .eventName(eventRequest.eventName())
                .eventDateTime(eventRequest.toDateTime())
                .build();
        return eventRepository.save(event);
    }

    public void update(Event event, UpdateEventRequest updateEventRequest) {
        event.update(updateEventRequest.eventName(), updateEventRequest.toDateTime());
    }

    public void delete(Event event) {
        startPointProcessor.deleteAllByEvent(event);
        reviewWriter.unlinkFromEvent(event);
        eventRepository.delete(event);
    }
}
