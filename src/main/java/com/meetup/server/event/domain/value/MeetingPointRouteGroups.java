package com.meetup.server.event.domain.value;

import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;

public record MeetingPointRouteGroups(
        MeetingPointRouteGroup byCoordinate,
        MeetingPointRouteGroup byPopularity
) {

    public static MeetingPointRouteGroups of(MeetingPointRouteGroup byCoordinate, MeetingPointRouteGroup byPopularity) {
        return new MeetingPointRouteGroups(byCoordinate, byPopularity);
    }
}
