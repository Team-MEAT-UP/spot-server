package com.meetup.server.global.clients.resilience4j;

import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class RetryEventConsumer implements RegistryEventConsumer<Retry> {

    @Override
    public void onEntryAddedEvent(EntryAddedEvent<Retry> entryAddedEvent) {
        entryAddedEvent.getAddedEntry().getEventPublisher()
                .onRetry(event -> log.warn("[Attempt: {}] {} retrying. failure reason: {} on {}",
                        event.getNumberOfRetryAttempts(),
                        event.getName(),
                        Optional.ofNullable(event.getLastThrowable()).map(Throwable::getMessage).orElse(null),
                        event.getCreationTime())
                )
                .onSuccess(event -> log.info("[Attempt: {}] {} retry successful on {}",
                        event.getNumberOfRetryAttempts(),
                        event.getName(),
                        event.getCreationTime())
                )
                .onError(event -> log.error("[Attempt: {}] {} retries failed. failure reason: {} on {}",
                        event.getNumberOfRetryAttempts(),
                        event.getName(),
                        Optional.ofNullable(event.getLastThrowable()).map(Throwable::getMessage).orElse(null),
                        event.getCreationTime())
                );
    }

    @Override
    public void onEntryRemovedEvent(EntryRemovedEvent<Retry> entryRemoveEvent) {

    }

    @Override
    public void onEntryReplacedEvent(EntryReplacedEvent<Retry> entryReplacedEvent) {

    }
}
