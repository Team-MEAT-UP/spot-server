package com.meetup.server.admin.dto.response;

public record DailyEventStatsResponse(
        long dailyEventCount,
        long dailyParticipantCount,
        long dailyKakaoInflowCount
) {
    public static DailyEventStatsResponse of(long eventCount, long participantCount, long dailyKakaoInflowCount) {
        return new DailyEventStatsResponse(eventCount, participantCount, dailyKakaoInflowCount);
    }
}
