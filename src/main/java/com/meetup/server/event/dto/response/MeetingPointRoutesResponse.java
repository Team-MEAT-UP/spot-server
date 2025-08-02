package com.meetup.server.event.dto.response;

import com.meetup.server.event.domain.Event;
import com.meetup.server.global.util.TimeUtil;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.util.UsernameExtractor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record MeetingPointRoutesResponse(
        String eventName,
        String eventDate,
        String eventTime,
        String eventMaker,
        String placeName,
        int peopleCount,
        List<MeetingPointRouteGroup> meetingPointRouteGroups
) {

    public static MeetingPointRoutesResponse of(List<MeetingPointResult> meetingPointResults, List<MeetingPointRouteGroup> meetingPointRouteGroups) {
        MeetingPointResult meetingPointResult = meetingPointResults.getFirst();

        Event event = meetingPointResult.event();
        LocalDateTime eventDateTime = event.getEventDateTime();

        List<StartPoint> startPoints = meetingPointResult.startPoints();

        String eventMaker = startPoints.stream()
                .min(Comparator.comparing(StartPoint::getCreatedAt))
                .map(UsernameExtractor::extractDisplayName)
                .orElse(null);

        int peopleCount = startPoints.size();

        return new MeetingPointRoutesResponse(
                event.getEventName(),
                TimeUtil.formatAsDate(eventDateTime),
                TimeUtil.formatAsTime(eventDateTime),
                eventMaker,
                event.getPlace().getName(),
                peopleCount,
                meetingPointRouteGroups
        );
    }
}
