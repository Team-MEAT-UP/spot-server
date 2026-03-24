package com.meetup.server.event.implement;

import java.util.UUID;
import java.util.concurrent.locks.Lock;

public interface EventLockManager {
    Lock getLock(UUID eventId);
}
