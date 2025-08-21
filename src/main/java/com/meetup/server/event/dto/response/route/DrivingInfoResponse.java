package com.meetup.server.event.dto.response.route;

import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public record DrivingInfoResponse(
        int taxi,     // 요금
        int toll,     // 통행료
        int duration, // 총 이동 소요 시간(min)
        int distance  // 총 이동 거리(m)
) {

    public static DrivingInfoResponse of(int taxi, int toll, int duration, int distance) {
        return DrivingInfoResponse.builder()
                .taxi(taxi)
                .toll(toll)
                .duration(durationConverter(duration))
                .distance(distance)
                .build();
    }

    public static DrivingInfoResponse from(KakaoMobilityResponse kakaoMobilityResponse) {
        return Optional.ofNullable(kakaoMobilityResponse)
                .map(KakaoMobilityResponse::routes)
                .filter(routes -> !routes.isEmpty())
                .map(List::getFirst)
                .map(KakaoMobilityResponse.Route::summary)
                .flatMap(summary ->
                        Optional.ofNullable(summary.fare())
                                .map(fare -> DrivingInfoResponse.of(
                                        fare.taxi(),
                                        fare.toll(),
                                        summary.duration(),
                                        summary.distance()
                                ))
                )
                .orElse(null);
    }

    private static int durationConverter(int duration) {
        return duration / 60;
    }
}
