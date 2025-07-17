package com.meetup.server.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeUtil {

    public static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    private static final DateTimeFormatter MM_DD_DOT_FORMATTER = DateTimeFormatter.ofPattern("MM.dd");

    private static final DateTimeFormatter HH_MM_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static int calculateDaysAgo(LocalDateTime createdAt) {
        return (int) ChronoUnit.DAYS.between(createdAt, LocalDateTime.now(KST_ZONE_ID));
    }

    public static int calculateHoursAgo(LocalDateTime createdAt) {
        return (int) ChronoUnit.HOURS.between(createdAt, LocalDateTime.now(KST_ZONE_ID));
    }

    public static String formatAsDate(LocalDateTime dateTime) {
        return dateTime.format(MM_DD_DOT_FORMATTER);
    }

    public static String formatAsTime(LocalDateTime dateTime) {
        return dateTime.format(HH_MM_FORMATTER);
    }
}
