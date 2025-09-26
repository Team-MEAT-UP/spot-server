package com.meetup.server.global.clients.kakao.mobility;

import com.meetup.server.global.clients.util.LimitRequestPerDay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KakaoMobilityClient {

    private final RestClient kakaoMobilityRestClient;

    @LimitRequestPerDay(
            key = "kakao-mobility",
            count = 10000
    )
    public KakaoMobilityResponse sendRequest(KakaoMobilityRequest request) {
        return kakaoMobilityRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("origin", request.origin())
                        .queryParam("destination", request.destination())
                        .queryParamIfPresent("waypoints", Optional.ofNullable(request.waypoints()))
                        .queryParamIfPresent("priority", Optional.ofNullable(request.priority()))
                        .queryParamIfPresent("avoid", Optional.ofNullable(request.avoid()))
                        .queryParamIfPresent("roadevent", Optional.ofNullable(request.roadEvent()))
                        .queryParamIfPresent("alternatives", Optional.ofNullable(request.alternatives()))
                        .queryParamIfPresent("road_details", Optional.ofNullable(request.roadDetails()))
                        .queryParamIfPresent("car_type", Optional.ofNullable(request.carType()))
                        .queryParamIfPresent("car_fuel", Optional.ofNullable(request.carFuel()))
                        .queryParamIfPresent("car_hipass", Optional.ofNullable(request.carHiPass()))
                        .queryParamIfPresent("summary", Optional.ofNullable(request.summary()))
                        .build())
                .retrieve()
                .body(KakaoMobilityResponse.class);
    }
}
