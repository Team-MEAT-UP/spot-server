package com.meetup.server.event.dto.response;

import java.util.List;

public record MeetingPointRoutesResponse(
        List<MeetingPointRouteGroup> meetingPointRouteGroups
) {
}
