package com.meetup.server.global.clients.ratelimit;

import com.meetup.server.global.clients.exception.ClientErrorType;
import com.meetup.server.global.clients.exception.ClientException;
import com.meetup.server.global.support.error.discord.DiscordAlarmSender;
import com.meetup.server.global.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiter implements RateLimiter {

    private final RedisTemplate<String, String> redisRateLimitTemplate;
    private final RedisScript<Long> redisRateLimitScript;
    private final DiscordAlarmSender discordAlarmSender;

    private static final String DISCORD_ALERT_SENT_KEY_PREFIX = "rate_limit_alert:";
    private static final String ODSAY_TRANSIT = "odsay-transit";
    private static final String KAKAO_MOBILITY = "kakao-mobility";

    @Override
    public void tryApiCall(LimitRequestPerDay limitRequestPerDay) {
        String key = limitRequestPerDay.key();
        int limitCount = limitRequestPerDay.count();

        Long result = redisRateLimitTemplate.execute(
                redisRateLimitScript,
                Collections.singletonList(key),
                String.valueOf(getTTL().toSeconds()),
                String.valueOf(limitCount)
        );

        if (result == -1) {
            sendRateLimitExceedAlert(key);
            return;
        }

        if (result >= limitCount - 50) {
            String alertSentKey = DISCORD_ALERT_SENT_KEY_PREFIX + key;
            Boolean isAlertSent = redisRateLimitTemplate.opsForValue().setIfAbsent(alertSentKey, "1", getTTL());
            if (Boolean.TRUE.equals(isAlertSent)) {
                sendRateLimitWarningAlert(key);
            }
        }
    }

    private Duration getTTL() {
        ZonedDateTime now = ZonedDateTime.now(TimeUtil.KST_ZONE_ID);
        ZonedDateTime midnight = now.plusDays(1).toLocalDate().atStartOfDay(TimeUtil.KST_ZONE_ID);
        return Duration.between(now, midnight);
    }

    private void sendRateLimitExceedAlert(String key) {
        switch (key) {
            case ODSAY_TRANSIT ->
                    discordAlarmSender.sendErrorAlert(new ClientException(ClientErrorType.ODSAY_EXCEED_RATE_LIMIT_PER_DAY));
            case KAKAO_MOBILITY ->
                    discordAlarmSender.sendErrorAlert(new ClientException(ClientErrorType.KAKAO_MOBILITY_EXCEED_RATE_LIMIT_PER_DAY));
        }
    }

    private void sendRateLimitWarningAlert(String key) {
        switch (key) {
            case ODSAY_TRANSIT ->
                    discordAlarmSender.sendErrorAlert(new ClientException(ClientErrorType.ODSAY_WARNING_RATE_LIMIT_PER_DAY));
            case KAKAO_MOBILITY ->
                    discordAlarmSender.sendErrorAlert(new ClientException(ClientErrorType.KAKAO_MOBILITY_WARNING_RATE_LIMIT_PER_DAY));
        }
    }
}
