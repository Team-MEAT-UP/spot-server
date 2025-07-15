package com.meetup.server.startpoint.implement;

import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.exception.StartPointErrorType;
import com.meetup.server.startpoint.exception.StartPointException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StartPointValidator {
    public void validateBelongsToEvent(UUID eventId, StartPoint startPoint) {
        if (!startPoint.getEvent().getEventId().equals(eventId)) {
            throw new StartPointException(StartPointErrorType.INVALID_START_POINT);
        }
    }
}
