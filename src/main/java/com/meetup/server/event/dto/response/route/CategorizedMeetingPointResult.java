package com.meetup.server.event.dto.response.route;

public record CategorizedMeetingPointResult(
        MeetingPointResult byCoordinate,
        MeetingPointResult byPopularity
) {
    public static CategorizedMeetingPointResult of(
            MeetingPointResult byCoordinate,
            MeetingPointResult byPopularity
    ) {
        return new CategorizedMeetingPointResult(byCoordinate, byPopularity);
    }
}
