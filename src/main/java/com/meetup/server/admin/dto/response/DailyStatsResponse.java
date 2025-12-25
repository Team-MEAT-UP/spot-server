package com.meetup.server.admin.dto.response;

public record DailyStatsResponse(
        Long dailyEventCount,
        Long dailyParticipantCount
) {
    public static DailyStatsResponse of(Long dailyEventCount, Long dailyParticipantCount) {
        return new DailyStatsResponse(dailyEventCount, dailyParticipantCount);
    }
}
