package com.meetup.server.event.infrastructure.redis;

import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;

import java.util.List;

public record MeetingPointRouteGroupsCache(
        List<MeetingPointRouteGroup> meetingPointRouteGroups
) {
}
