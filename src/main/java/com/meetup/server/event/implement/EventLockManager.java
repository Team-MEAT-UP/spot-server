package com.meetup.server.event.implement;

import com.meetup.server.event.domain.value.ParticipantKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class EventLockManager {
    private final ConcurrentHashMap<UUID, ReentrantLock> eventLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Set<ParticipantKey>> participantLocks = new ConcurrentHashMap<>();

    public ReentrantLock getLock(UUID eventId) {
        return eventLocks.computeIfAbsent(eventId, key -> new ReentrantLock());
    }

    public boolean markParticipantInProcess(UUID eventId, ParticipantKey participantId) {
        log.debug("[EVENT LOCK] mark participant lock , eventId: {}, participantId: {}", eventId, participantId);

        return participantLocks
                .computeIfAbsent(eventId, key -> ConcurrentHashMap.newKeySet())
                .add(participantId);
    }

    public void removeParticipantInProcess(UUID eventId, ParticipantKey participantId) {
        participantLocks
                .computeIfAbsent(eventId, key -> ConcurrentHashMap.newKeySet())
                .remove(participantId);

        log.debug("[EVENT LOCK] remove participant lock , eventId: {}, participantId: {}", eventId, participantId);
    }
}
