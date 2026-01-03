package com.meetup.server.admin.dto.response;

public record DailyUserStatsResponse(
        long dailyLoginUserCount,
        long dailyRegisterUserCount
) {
    public static DailyUserStatsResponse of(long loginCount, long registerCount) {
        return new DailyUserStatsResponse(loginCount, registerCount);
    }
}
