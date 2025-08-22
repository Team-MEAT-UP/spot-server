package com.meetup.server.event.domain.value;

import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import lombok.Builder;

import java.util.List;

@Builder
public record MeetingPointRouteGroups(
        List<MeetingPointRouteGroup> meetingPointRouteGroups
) {

    public static MeetingPointRouteGroups from(List<MeetingPointRouteGroup> meetingPointRouteGroupList) {
        List<MeetingPointRouteGroup> meetingPointRouteGroups = meetingPointRouteGroupList.stream()
                .map(group -> new MeetingPointRouteGroup(
                        group.subwayId(),
                        group.averageTime(),
                        group.meetingPoint(),
                        group.routeResponse(),
                        group.parkingLot()
                ))
                .toList();

        return new MeetingPointRouteGroups(meetingPointRouteGroups);
    }
}

