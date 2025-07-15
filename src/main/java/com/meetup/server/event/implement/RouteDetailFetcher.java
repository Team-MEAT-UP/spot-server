package com.meetup.server.event.implement;

import com.meetup.server.event.dto.response.DrivingInfoResponse;
import com.meetup.server.event.dto.response.RouteResponse;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchResponse;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.exception.StartPointErrorType;
import com.meetup.server.startpoint.exception.StartPointException;
import com.meetup.server.startpoint.util.RouteExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteDetailFetcher {

    private final RouteApiCaller routeApiCaller;

    public RouteResponse fetch(
            StartPoint startPoint,
            String startX, String startY, String endX, String endY
    ) {
        OdsayTransitRouteSearchResponse transitRoute = routeApiCaller.getTransitRoute(startX, startY, endX, endY);
        KakaoMobilityResponse drivingRoute = routeApiCaller.getDrivingRoute(startX, startY, endX, endY);

        if (transitRoute == null) {
            throw new StartPointException(StartPointErrorType.ODSAY_ERROR);
        }
        if (drivingRoute == null) {
            throw new StartPointException(StartPointErrorType.KAKAO_ERROR);
        }

        int transitTotalTime = RouteExtractor.extractValidTransitTotalTime(transitRoute);
        int drivingTotalTime = RouteExtractor.extractValidDrivingTotalTime(DrivingInfoResponse.from(drivingRoute));

        return RouteResponse.of(startPoint, startPoint.getUser(), transitRoute, drivingRoute, transitTotalTime, drivingTotalTime);
    }
}
