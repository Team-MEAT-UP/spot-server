package com.meetup.server.global.clients.ratelimit;

import com.meetup.server.global.clients.exception.ClientErrorType;
import com.meetup.server.global.clients.exception.ClientException;
import com.meetup.server.global.support.error.GlobalErrorType;
import com.meetup.server.global.support.error.discord.DiscordAlarmSender;
import com.meetup.server.global.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaRateLimiter implements RateLimiter {

    private final ApiCallLimitRepository apiCallLimitRepository;
    private final DiscordAlarmSender discordAlarmSender;

    private static final String ODSAY_TRANSIT = "odsay-transit";
    private static final String KAKAO_MOBILITY = "kakao-mobility";
    private static final int RATE_LIMIT_WARNING_THRESHOLD = 50;

    @Override
    @Transactional
    public void tryApiCall(LimitRequestPerDay limitRequestPerDay) {
        String key = limitRequestPerDay.key();
        int limitCount = limitRequestPerDay.count();
        LocalDate today = ZonedDateTime.now(TimeUtil.KST_ZONE_ID).toLocalDate();

        apiCallLimitRepository.initApiCallLimit(key, today);

        ApiCallLimit apiCallLimit = apiCallLimitRepository.findWithLockByApiNameAndCallDate(key, today)
                .orElseThrow(() -> new RuntimeException(GlobalErrorType.INTERNAL_ERROR.getMessage()));

        if (apiCallLimit.getCount() >= limitCount) {
            return;
        }

        apiCallLimit.increment();

        if (apiCallLimit.getCount() == limitCount) {
            sendRateLimitExceedAlert(key);
            return;
        }

        if (apiCallLimit.getCount() == limitCount - RATE_LIMIT_WARNING_THRESHOLD) {
            sendRateLimitWarningAlert(key);
        }
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
