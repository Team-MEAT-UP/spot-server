package com.meetup.server.event.domain.value;

import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;

import java.util.List;

public record MeetingPointRouteGroups(
        List<MeetingPointRouteGroup> meetingPointRouteGroups
) {
}
