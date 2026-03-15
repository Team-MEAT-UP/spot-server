package com.meetup.server.global.clients.ratelimit;

import com.meetup.server.global.clients.exception.ClientException;
import com.meetup.server.global.support.error.discord.DiscordAlarmSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RateLimitAlertEventListener {

    private final DiscordAlarmSender discordAlarmSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRateLimitAlert(RateLimitAlertEvent event) {
        discordAlarmSender.sendErrorAlert(new ClientException(event.clientErrorType()));
    }
}
