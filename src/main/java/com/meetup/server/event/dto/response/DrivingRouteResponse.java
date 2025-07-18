package com.meetup.server.event.dto.response;

import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse.Route;
import com.meetup.server.global.util.Coordinate;

import java.util.ArrayList;
import java.util.List;

public record DrivingRouteResponse(
        String name,
        List<Coordinate> coordinates
) {
    public static List<DrivingRouteResponse> from(KakaoMobilityResponse kakaoMobilityResponse) {
        if (kakaoMobilityResponse == null || kakaoMobilityResponse.routes() == null || kakaoMobilityResponse.routes().isEmpty()) {
            return null;
        }

        Route route = kakaoMobilityResponse.routes().getFirst();

        return route.sections().stream()
                .flatMap(section -> section.roads().stream())
                .map(road -> {
                    double[] vertexes = road.vertexes();
                    List<Coordinate> coordinateList = new ArrayList<>();

                    for (int i = 0; i < vertexes.length - 1; i += 2) {
                        coordinateList.add(Coordinate.of(vertexes[i], vertexes[i + 1]));
                    }

                    return new DrivingRouteResponse(road.name(), coordinateList);
                })
                .toList();
    }
}
