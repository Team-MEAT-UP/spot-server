package com.meetup.server.admin.dto.response;

import java.time.LocalDate;

public record DailyActivationStatsResponse(
        LocalDate date,
        long totalEvents,
        long confirmedPlaceEvents,
        long confirmedEventsWithKakao,
        double activationRate,
        double activationRateWithKakao
) {
    public static DailyActivationStatsResponse of(
            LocalDate date, 
            long totalEvents, 
            long confirmedPlaceEvents,
            long confirmedEventsWithKakao
    ) {
        double rate = totalEvents > 0 ? (double) confirmedPlaceEvents / totalEvents * 100 : 0.0;
        double rateWithKakao = totalEvents > 0 ? (double) confirmedEventsWithKakao / totalEvents * 100 : 0.0;
        return new DailyActivationStatsResponse(
                date, 
                totalEvents, 
                confirmedPlaceEvents,
                confirmedEventsWithKakao,
                Math.round(rate * 100.0) / 100.0,
                Math.round(rateWithKakao * 100.0) / 100.0
        );
    }
}
