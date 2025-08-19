package com.meetup.server.startpoint.infrastructure.querydsl;

import com.meetup.server.startpoint.infrastructure.querydsl.projection.EventHistory;
import com.meetup.server.startpoint.infrastructure.querydsl.projection.Participant;
import com.meetup.server.startpoint.infrastructure.querydsl.projection.ParticipantCount;

import java.util.List;
import java.util.UUID;

public interface StartPointCustomRepository {
    List<EventHistory> findEventHistories(Long userId, UUID lastViewedEventId, int size);

    List<Participant> findParticipantsWithImageUrls(List<UUID> eventIds);

    List<ParticipantCount> findParticipantsCounts(List<UUID> eventIds);
}
