package com.meetup.server.event.infrastructure.redis;

import com.meetup.server.event.implement.EventLockManager;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.locks.Lock;

@Component
@RequiredArgsConstructor
public class RedissonEventLockManager implements EventLockManager {
    private final RedissonClient redissonClient;

    @Override
    public Lock getLock(UUID eventId) {
        return redissonClient.getLock("event:lock:" + eventId);
    }
}
