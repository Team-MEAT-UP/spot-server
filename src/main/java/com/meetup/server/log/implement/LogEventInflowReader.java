package com.meetup.server.log.implement;

import com.meetup.server.log.domain.type.InflowType;
import com.meetup.server.log.infrastructure.jpa.LogEventInflowRepository;
import com.meetup.server.log.infrastructure.jpa.projection.EventInflowCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LogEventInflowReader {

    private final LogEventInflowRepository logEventInflowRepository;

    public long readDailyEventInflowCount(InflowType inflowType, LocalDate todayDate) {
        LocalDateTime startDateTime = todayDate.atStartOfDay();
        LocalDateTime endDateTime = todayDate.atTime(LocalTime.MAX);
        return logEventInflowRepository.countByInflowTypeAndCreatedAtBetween(inflowType, startDateTime, endDateTime);
    }

    public List<EventInflowCount> readEventInflowCountByEventIds(InflowType inflowType, List<UUID> eventIds) {
        return logEventInflowRepository.findCountsByInflowTypeAndEventIds(inflowType, eventIds);
    }
}
