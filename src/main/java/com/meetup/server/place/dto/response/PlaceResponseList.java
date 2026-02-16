package com.meetup.server.place.dto.response;

import com.meetup.server.event.domain.Event;
import com.meetup.server.subway.domain.Subway;

import java.util.List;

public record PlaceResponseList(
        String eventName,
        String middlePointName,
        PlaceResponse confirmedPlaceResponse,
        List<PlaceResponse> placeResponses
) {
    public static PlaceResponseList of(Event event, Subway subway, PlaceResponse confirmedPlaceResponse, List<PlaceResponse> placeResponses) {
        return new PlaceResponseList(
                event.getEventName(),
                subway.getName(),
                confirmedPlaceResponse,
                placeResponses
        );
    }
}
