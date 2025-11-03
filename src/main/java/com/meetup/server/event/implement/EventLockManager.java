package com.meetup.server.event.implement;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Getter
@Component
public class EventLockManager {
    private final ConcurrentHashMap<UUID, ReentrantLock> eventLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Set<Object>> participantLocks = new ConcurrentHashMap<>();

    public ReentrantLock getLock(UUID eventId) {
        return eventLocks.computeIfAbsent(eventId, key -> new ReentrantLock());
    }

    public void markParticipantInProcess(UUID eventId, Object participantId) {
        participantLocks
                .computeIfAbsent(eventId, key -> ConcurrentHashMap.newKeySet())
                .add(participantId);
        log.debug("[EVENT LOCK] mark participant lock , eventId: {}, participantId: {}", eventId, participantId);
    }

    public void removeParticipantInProcess(UUID eventId, Object participantId) {
        participantLocks
                .computeIfAbsent(eventId, key -> ConcurrentHashMap.newKeySet())
                .remove(participantId);

        log.debug("[EVENT LOCK] remove participant lock , eventId: {}, participantId: {}", eventId, participantId);
    }
}
