package com.meetup.server.event.implement.route;

import com.meetup.server.event.dto.response.route.DrivingInfoResponse;
import com.meetup.server.event.dto.response.route.RouteResponse;
import com.meetup.server.event.util.RouteExtractor;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchResponse;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.subway.domain.Subway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteFetcher {

    private final RouteApiCaller routeApiCaller;

    public RouteResponse fetch(StartPoint startPoint, Subway subway) {
        String startX = String.valueOf(startPoint.getLocation().getRoadLongitude());
        String startY = String.valueOf(startPoint.getLocation().getRoadLatitude());
        String endX = String.valueOf(subway.getLocation().getRoadLongitude());
        String endY = String.valueOf(subway.getLocation().getRoadLatitude());

        OdsayTransitRouteSearchResponse transitRoute = null;
        KakaoMobilityResponse drivingRoute = null;
        int transitTotalTime = 0;
        int drivingTotalTime = 0;

        if (startPoint.isTransit()) {
            transitRoute = fetchTransitRoute(startX, startY, endX, endY);
            transitTotalTime = RouteExtractor.extractValidTransitTotalTime(transitRoute);
        } else {
            drivingRoute = fetchDrivingRoute(startX, startY, endX, endY);
            drivingTotalTime = RouteExtractor.extractValidDrivingTotalTime(DrivingInfoResponse.from(drivingRoute));
        }

        return RouteResponse.of(startPoint, transitRoute, drivingRoute, transitTotalTime, drivingTotalTime);
    }

    private OdsayTransitRouteSearchResponse fetchTransitRoute(String startX, String startY, String endX, String endY) {
        try {
            return routeApiCaller.getTransitRoute(startX, startY, endX, endY);
        } catch (Exception e) {
            log.warn("대중교통 경로 조회 실패: startX={}, startY={}, endX={}, endY={}", startX, startY, endX, endY, e);
            return null;
        }
    }

    private KakaoMobilityResponse fetchDrivingRoute(String startX, String startY, String endX, String endY) {
        try {
            return routeApiCaller.getDrivingRoute(startX, startY, endX, endY);
        } catch (Exception e) {
            log.warn("자동차 경로 조회 실패: startX={}, startY={}, endX={}, endY={}", startX, startY, endX, endY, e);
            return null;
        }
    }
}
