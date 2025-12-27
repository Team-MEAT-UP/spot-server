package com.meetup.server.admin.dto.response;

public record DailyStatsResponse(
        long dailyEventCount,
        long dailyParticipantCount
) {
    public static DailyStatsResponse of(long dailyEventCount, long dailyParticipantCount) {
        return new DailyStatsResponse(dailyEventCount, dailyParticipantCount);
    }
}
