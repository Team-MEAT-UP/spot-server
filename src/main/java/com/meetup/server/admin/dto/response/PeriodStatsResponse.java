package com.meetup.server.admin.dto.response;

import java.util.List;

public record PeriodStatsResponse(
        List<DailyActivationStatsResponse> activationStats,
        List<DailyRetentionStatsResponse> retentionStats,
        long overallActivationRate,
        long overallActivationRateWithKakao,
        long overallRetentionRate,
        long overallRetentionRateWithKakao
) {
    public static PeriodStatsResponse of(
            List<DailyActivationStatsResponse> activationStats,
            List<DailyRetentionStatsResponse> retentionStats
    ) {
        long totalEvents = activationStats.stream().mapToLong(DailyActivationStatsResponse::totalEvents).sum();
        long confirmedEvents = activationStats.stream().mapToLong(DailyActivationStatsResponse::confirmedPlaceEvents).sum();
        long confirmedEventsWithKakao = activationStats.stream().mapToLong(DailyActivationStatsResponse::confirmedEventsWithKakao).sum();
        long overallActivationRate = totalEvents > 0 ? (long) ((double) confirmedEvents / totalEvents * 100) : 0;
        long overallActivationRateWithKakao = totalEvents > 0 ? (long) ((double) confirmedEventsWithKakao / totalEvents * 100) : 0;

        long totalUsers = retentionStats.stream().mapToLong(DailyRetentionStatsResponse::totalUsersWithEvents).sum();
        long retainedUsers = retentionStats.stream().mapToLong(DailyRetentionStatsResponse::retainedUsers).sum();
        long retainedUsersWithKakao = retentionStats.stream().mapToLong(DailyRetentionStatsResponse::retainedUsersWithKakao).sum();
        long overallRetentionRate = totalUsers > 0 ? (long) ((double) retainedUsers / totalUsers * 100) : 0;
        long overallRetentionRateWithKakao = totalUsers > 0 ? (long) ((double) retainedUsersWithKakao / totalUsers * 100) : 0;

        return new PeriodStatsResponse(activationStats, retentionStats, overallActivationRate, overallActivationRateWithKakao, overallRetentionRate, overallRetentionRateWithKakao);
    }
}
