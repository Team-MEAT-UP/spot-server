package com.meetup.server.startpoint.listener;

import com.meetup.server.event.domain.value.StartPointChangedEvent;
import com.meetup.server.event.implement.EventLockManager;
import com.meetup.server.event.implement.EventProcessor;
import com.meetup.server.event.implement.route.RouteProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartPointEventListener {

    private final RouteProcessor routeProcessor;
    private final EventLockManager eventLockManager;
    private final EventProcessor eventProcessor;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStartPointChanged(StartPointChangedEvent event) {
        ReentrantLock lock = eventLockManager.getLock(event.eventId());

        lock.lock();
        try {
            eventProcessor.deleteRoute(event.eventId());
            routeProcessor.deleteCache(event.eventId());
            log.info("[DELETE ROUTE/CACHE AFTER COMMIT] eventId: {}", event.eventId());
        } finally {
            lock.unlock();
        }
    }
}
