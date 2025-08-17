package com.meetup.server.event.dto.response.route;

import com.meetup.server.subway.domain.Subway;

public record MeetingPoint(
        String endStationName,  //중간지점 역
        double endLongitude, //경도(:longitude)
        double endLatitude //위도(:latitude)
) {
    public static MeetingPoint from(Subway subway) {
        return new MeetingPoint(subway.getName(), subway.getLocation().getRoadLongitude(), subway.getLocation().getRoadLatitude());
    }
}
