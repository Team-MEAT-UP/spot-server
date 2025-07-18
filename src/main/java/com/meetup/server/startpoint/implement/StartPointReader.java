package com.meetup.server.startpoint.implement;

import com.meetup.server.event.domain.Event;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.exception.StartPointErrorType;
import com.meetup.server.startpoint.exception.StartPointException;
import com.meetup.server.startpoint.persistence.StartPointRepository;
import com.meetup.server.startpoint.persistence.projection.ParticipantCount;
import com.meetup.server.startpoint.persistence.projection.Participant;
import com.meetup.server.startpoint.persistence.projection.EventHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StartPointReader {

    private final StartPointRepository startPointRepository;

    public List<StartPoint> readAll(Event event) {
        return startPointRepository.findAllByEvent(event);
    }

    public StartPoint read(UUID startPointId) {
        return startPointRepository.findById(startPointId)
                .orElseThrow(() -> new StartPointException(StartPointErrorType.PLACE_NOT_FOUND));
    }

    public List<EventHistory> readEventHistories(Long userId, UUID lastViewedEventId, int size) {
        return startPointRepository.findEventHistories(userId, lastViewedEventId, size);
    }

    public List<Participant> readParticipantsWithUrls(List<UUID> eventIds) {
        return startPointRepository.findParticipantsWithImageUrls(eventIds);
    }

    public List<ParticipantCount> readParticipantCounts(List<UUID> eventIds) {
        return startPointRepository.findParticipantsCounts(eventIds);
    }
}
