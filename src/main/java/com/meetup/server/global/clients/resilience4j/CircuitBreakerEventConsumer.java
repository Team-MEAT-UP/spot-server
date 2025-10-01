package com.meetup.server.global.clients.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CircuitBreakerEventConsumer implements RegistryEventConsumer<CircuitBreaker> {

    @Override
    public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
        entryAddedEvent.getAddedEntry().getEventPublisher()
                .onFailureRateExceeded(event -> log.warn("{} failure rate {}% on {}",
                        event.getCircuitBreakerName(),
                        event.getFailureRate(),
                        event.getCreationTime())
                )
                .onError(event -> log.error("{} error with duration {}ms",
                        event.getCircuitBreakerName(),
                        event.getElapsedDuration().toMillis())
                )
                .onStateTransition(event -> log.info("{} state transition from {} to {} on {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState(),
                        event.getCreationTime())
                );
    }

    @Override
    public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
    }

    @Override
    public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
    }
}
