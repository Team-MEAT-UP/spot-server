package com.meetup.server.startpoint.application;

import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityClient;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityRequest;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchClient;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchRequest;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchResponse;
import com.meetup.server.startpoint.domain.type.KakaoMobilityResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteFacadeService {

    private final OdsayTransitRouteSearchClient odsayTransitRouteSearchClient;
    private final KakaoMobilityClient kakaoMobilityClient;

    public OdsayTransitRouteSearchResponse getTransitRoute(String startX, String startY, String endX, String endY) {
        OdsayTransitRouteSearchResponse response = odsayTransitRouteSearchClient.sendRequest(
                OdsayTransitRouteSearchRequest.builder()
                        .sx(startX)
                        .sy(startY)
                        .ex(endX)
                        .ey(endY)
                        .build()
        );
        return response;
    }

    public KakaoMobilityResponse getDrivingRoute(String startX, String startY, String endX, String endY) {
        KakaoMobilityResponse response = kakaoMobilityClient.sendRequest(
                KakaoMobilityRequest.builder()
                        .origin(startX + "," + startY)
                        .destination(endX + "," + endY)
                        .build()

        );
        log.info("response: {}", response);

        if (response.routes() != null) {
            for (KakaoMobilityResponse.Route route : response.routes()) {
                if (KakaoMobilityResultCode.SUCCESS.matches(route.resultCode())) {
                    return new KakaoMobilityResponse(response.transId(), List.of(route));
                }
            }
        }

        return response;
    }
}
