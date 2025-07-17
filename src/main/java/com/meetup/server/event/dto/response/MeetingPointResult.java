package com.meetup.server.event.dto.response;

import com.meetup.server.event.domain.Event;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.subway.domain.Subway;
import lombok.Builder;

import java.util.List;

@Builder
public record MeetingPointResult(
        Event event,
        List<StartPoint> startPoints,
        Subway subway
) {
    public static MeetingPointResult of(Event event, List<StartPoint> startPoints, Subway subway) {
        return MeetingPointResult.builder()
                .event(event)
                .startPoints(startPoints)
                .subway(subway)
                .build();
    }
}

