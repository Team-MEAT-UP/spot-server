package com.meetup.server.admin.dto.response;

public record DailyEventStatsResponse(
        long dailyEventCount,
        long dailyParticipantCount
) {
    public static DailyEventStatsResponse of(long eventCount, long participantCount) {
        return new DailyEventStatsResponse(eventCount, participantCount);
    }
}
