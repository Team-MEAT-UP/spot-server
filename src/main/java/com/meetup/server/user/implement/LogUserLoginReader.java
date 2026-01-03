package com.meetup.server.user.implement;

import com.meetup.server.user.infrastructure.jpa.LogUserLoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class LogUserLoginReader {

    private final LogUserLoginRepository logUserLoginRepository;

    public long readDailyLoginUserCount(LocalDate todayDate) {
        LocalDateTime startDateTime = todayDate.atStartOfDay();
        LocalDateTime endDateTime = todayDate.atTime(LocalTime.MAX);
        return logUserLoginRepository.countUniqueLoginUsers(startDateTime, endDateTime);
    }
}
