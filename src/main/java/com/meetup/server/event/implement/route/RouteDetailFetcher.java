package com.meetup.server.event.implement.route;

import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteDetailFetcher {

    private final RouteApiCaller routeApiCaller;

    public OdsayTransitRouteSearchResponse fetchTransitRoute(String startX, String startY, String endX, String endY) {
        try {
            return routeApiCaller.getTransitRoute(startX, startY, endX, endY);
        } catch (Exception e) {
            log.warn("대중교통 경로 조회 실패: startX={}, startY={}, endX={}, endY={}", startX, startY, endX, endY, e);
            return null;
        }
    }

    public KakaoMobilityResponse fetchDrivingRoute(String startX, String startY, String endX, String endY) {
        try {
            return routeApiCaller.getDrivingRoute(startX, startY, endX, endY);
        } catch (Exception e) {
            log.warn("자동차 경로 조회 실패: startX={}, startY={}, endX={}, endY={}", startX, startY, endX, endY, e);
            return null;
        }
    }
}
