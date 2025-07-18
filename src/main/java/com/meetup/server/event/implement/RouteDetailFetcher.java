package com.meetup.server.event.implement;

import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchResponse;
import com.meetup.server.startpoint.exception.StartPointErrorType;
import com.meetup.server.startpoint.exception.StartPointException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteDetailFetcher {

    private final RouteApiCaller routeApiCaller;

    public OdsayTransitRouteSearchResponse fetchTransitRoute(String startX, String startY, String endX, String endY) {
        OdsayTransitRouteSearchResponse transitRoute = routeApiCaller.getTransitRoute(startX, startY, endX, endY);
        return transitRoute;
    }

    public KakaoMobilityResponse fetchDrivingRoute(String startX, String startY, String endX, String endY) {
        KakaoMobilityResponse drivingRoute = routeApiCaller.getDrivingRoute(startX, startY, endX, endY);
        return drivingRoute;
    }
}
