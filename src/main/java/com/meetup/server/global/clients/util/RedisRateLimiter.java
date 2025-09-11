package com.meetup.server.global.clients.util;

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
            discordAlarmSender.sendErrorAlert(new ClientException(ClientErrorType.EXCEED_RATE_LIMIT_PER_DAY));
            return;
        }

        if (result >= limitCount - 50) {
            String alertSentKey = DISCORD_ALERT_SENT_KEY_PREFIX + key;
            Boolean isAlertSent = redisRateLimitTemplate.opsForValue().setIfAbsent(alertSentKey, "1", getTTL());
            if (Boolean.TRUE.equals(isAlertSent)) {
                discordAlarmSender.sendErrorAlert(new ClientException(ClientErrorType.WARNING_RATE_LIMIT_PER_DAY));
            }
        }
    }

    private Duration getTTL() {
        ZonedDateTime now = ZonedDateTime.now(TimeUtil.KST_ZONE_ID);
        ZonedDateTime midnight = now.plusDays(1).toLocalDate().atStartOfDay(TimeUtil.KST_ZONE_ID);
        return Duration.between(now, midnight);
    }
}
