package com.meetup.server.event.dto.response.route;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.util.UsernameExtractor;
import com.meetup.server.global.util.TimeUtil;
import com.meetup.server.place.domain.Place;
import com.meetup.server.startpoint.domain.StartPoint;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record MeetingPointRoutesResponse(
        String eventName,
        String eventDate,
        String eventTime,
        String eventMaker,
        String placeName,
        int peopleCount,
        List<MeetingPointRouteGroup> meetingPointRouteGroups
) {

    public static MeetingPointRoutesResponse of(Event event, List<StartPoint> startPoints, List<MeetingPointRouteGroup> meetingPointRouteGroups) {
        return new MeetingPointRoutesResponse(
                event.getEventName(),
                TimeUtil.formatAsDashDate(event.getEventDateTime()),
                TimeUtil.formatAsTime(event.getEventDateTime()),
                extractEventMaker(startPoints),
                extractPlaceName(event),
                startPoints.size(),
                meetingPointRouteGroups
        );
    }

    private static String extractPlaceName(Event event) {
        return Optional.ofNullable(event.getPlace())
                .map(Place::getName)
                .orElse(null);
    }

    private static String extractEventMaker(List<StartPoint> startPoints) {
        return startPoints.stream()
                .min(Comparator.comparing(StartPoint::getCreatedAt))
                .map(UsernameExtractor::extractDisplayName)
                .orElse(null);
    }

    public MeetingPointRoutesResponse withEvent(String eventName, LocalDateTime eventDateTime) {
        return new MeetingPointRoutesResponse(
                eventName,
                TimeUtil.formatAsDashDate(eventDateTime),
                TimeUtil.formatAsTime(eventDateTime),
                this.eventMaker(),
                this.placeName(),
                this.peopleCount(),
                this.meetingPointRouteGroups()
        );
    }
}
