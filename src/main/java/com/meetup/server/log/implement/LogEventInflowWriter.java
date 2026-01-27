package com.meetup.server.log.implement;

import com.meetup.server.log.domain.LogEventInflow;
import com.meetup.server.log.domain.type.InflowType;
import com.meetup.server.log.infrastructure.jpa.LogEventInflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventInflowWriter {

    private final LogEventInflowRepository logEventInflowRepository;

    @Async
    public void save(InflowType inflowType, UUID eventId, Long userId, String ipAddress, String userAgent) {
        try {
            logEventInflowRepository.save(
                    LogEventInflow.create(inflowType, eventId, userId, ipAddress, userAgent)
            );
        } catch (Exception e) {
            log.error("[LogEventInflowWriter]: 유저 이벤트 유입 로그 저장에 실패했습니다. eventId: {}, userId: {}", eventId, userId, e);
        }
    }
}
