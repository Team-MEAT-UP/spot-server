package com.meetup.server.event.implement;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class EventLockManager {
    private final ConcurrentHashMap<UUID, ReentrantLock> eventLocks = new ConcurrentHashMap<>();

    public ReentrantLock getLock(UUID eventId) {
        return eventLocks.computeIfAbsent(eventId, k -> new ReentrantLock());
    }
}
