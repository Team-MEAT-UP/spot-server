package com.meetup.server.event.implement.route;

import com.meetup.server.event.dto.response.route.DrivingInfoResponse;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.route.RouteResponse;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchResponse;
import com.meetup.server.parkinglot.implement.ParkingLotFinder;
import com.meetup.server.parkinglot.infrastructure.jpa.projection.ClosestParkingLot;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.event.util.RouteExtractor;
import com.meetup.server.subway.domain.Subway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteAssembler {

    private final ParkingLotFinder parkingLotFinder;
    private final RouteDetailFetcher routeDetailFetcher;

    public MeetingPointRouteGroup assemble(List<StartPoint> startPointList, Subway subway) {
        List<RouteResponse> routeList = startPointList.stream()
                .map(startPoint -> {
                    String startX = String.valueOf(startPoint.getLocation().getRoadLongitude());
                    String startY = String.valueOf(startPoint.getLocation().getRoadLatitude());
                    String endX = String.valueOf(subway.getLocation().getRoadLongitude());
                    String endY = String.valueOf(subway.getLocation().getRoadLatitude());

                    OdsayTransitRouteSearchResponse transitRoute = null;
                    KakaoMobilityResponse drivingRoute = null;
                    int transitTotalTime = 0;
                    int drivingTotalTime = 0;

                    if (startPoint.isTransit()) {
                        transitRoute = routeDetailFetcher.fetchTransitRoute(startX, startY, endX, endY);
                        transitTotalTime = RouteExtractor.extractValidTransitTotalTime(transitRoute);
                    } else {
                        drivingRoute = routeDetailFetcher.fetchDrivingRoute(startX, startY, endX, endY);
                        drivingTotalTime = RouteExtractor.extractValidDrivingTotalTime(DrivingInfoResponse.from(drivingRoute));
                    }

                    return RouteResponse.of(startPoint, transitRoute, drivingRoute, transitTotalTime, drivingTotalTime);
                })
                .collect(Collectors.toList());

        ClosestParkingLot closestParkingLot = parkingLotFinder.findClosestParkingLot(subway.getPoint());
        return MeetingPointRouteGroup.of(routeList, subway, closestParkingLot);
    }
}
