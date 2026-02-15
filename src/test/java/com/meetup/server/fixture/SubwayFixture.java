package com.meetup.server.fixture;

import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.startpoint.domain.type.Location;
import com.meetup.server.subway.domain.Subway;

public class SubwayFixture {

    public static final int SUBWAY_ID = 1;

    public static Subway getSubway() {
        return Subway.builder()
                .name("강남")
                .code("222")
                .line("2")
                .location(Location.of(127.02800140627488, 37.49808633653005))
                .point(CoordinateUtil.createPoint(127.02800140627488, 37.49808633653005))
                .build();
    }
}
