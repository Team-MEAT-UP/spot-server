package com.meetup.server.admin.dto.response;

import java.time.LocalDate;

public record DailyRetentionStatsResponse(
        LocalDate date,
        long totalUsersWithEvents,
        long retainedUsers,
        long retainedUsersWithKakao,
        double retentionRate,
        double retentionRateWithKakao
) {
    public static DailyRetentionStatsResponse of(LocalDate date, long totalUsersWithEvents, long retainedUsers, long retainedUsersWithKakao) {
        double retentionRate = totalUsersWithEvents > 0 ? (double) retainedUsers / totalUsersWithEvents * 100 : 0.0;
        double retentionRateWithKakao = totalUsersWithEvents > 0 ? (double) retainedUsersWithKakao / totalUsersWithEvents * 100 : 0.0;
        return new DailyRetentionStatsResponse(date, totalUsersWithEvents, retainedUsers, retainedUsersWithKakao, Math.round(retentionRate * 100.0) / 100.0, Math.round(retentionRateWithKakao * 100.0) / 100.0);
    }
}
