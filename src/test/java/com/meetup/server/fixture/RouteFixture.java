package com.meetup.server.fixture;

import com.meetup.server.event.domain.type.TrafficType;
import com.meetup.server.event.dto.response.route.DrivingInfoResponse;
import com.meetup.server.event.dto.response.route.DrivingRouteResponse;
import com.meetup.server.event.dto.response.route.TransitRouteResponse;
import com.meetup.server.global.util.Coordinate;
import com.meetup.server.subway.dto.response.PassStopList;
import com.meetup.server.subway.dto.response.Stations;

import java.util.List;

public class RouteFixture {

    public static TransitRouteResponse getTransitRoute() {
        Stations station = new Stations(0, "강남", "127.027636", "37.497985");
        PassStopList passStopList = new PassStopList(List.of(station));

        return TransitRouteResponse.builder()
                .trafficType(TrafficType.SUBWAY)
                .startExitNo("3")
                .endExitNo("5")
                .distance(3500.0)
                .laneName("2")
                .startBoardName("강남")
                .endBoardName("교대")
                .stationCount(2)
                .passStopList(passStopList)
                .sectionTime(10)
                .build();
    }

    public static DrivingInfoResponse getDrivingInfo() {
        return new DrivingInfoResponse(
                10000,
                3500,
                30,
                15500
        );
    }

    public static DrivingRouteResponse getDrivingRoute() {
        List<Coordinate> coordinates = List.of(
                Coordinate.of(127.043999, 37.510297),
                Coordinate.of(127.030000, 37.515000),
                Coordinate.of(127.021385, 37.511108)
        );

        return new DrivingRouteResponse(
                "테헤란로/강남대로",
                coordinates
        );
    }
}
