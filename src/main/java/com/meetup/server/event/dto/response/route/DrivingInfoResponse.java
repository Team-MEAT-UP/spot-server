package com.meetup.server.event.dto.response.route;

import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import lombok.Builder;

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
        if (kakaoMobilityResponse == null || kakaoMobilityResponse.routes() == null || kakaoMobilityResponse.routes().isEmpty()) {
            return null;
        }

        KakaoMobilityResponse.Route route = kakaoMobilityResponse.routes().getFirst();
        KakaoMobilityResponse.Summary summary = route.summary();
        KakaoMobilityResponse.Fare fare = summary.fare();

        return DrivingInfoResponse.of(fare.taxi(), fare.toll(), summary.duration(), summary.distance());
    }

    private static int durationConverter(int duration) {
        return duration / 60;
    }
}
