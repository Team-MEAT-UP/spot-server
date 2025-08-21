package com.meetup.server.event.dto.response.route;

import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse.Route;
import com.meetup.server.global.util.Coordinate;

import java.util.*;

public record DrivingRouteResponse(
        String name,
        List<Coordinate> coordinates
) {
    public static List<DrivingRouteResponse> from(KakaoMobilityResponse kakaoMobilityResponse) {
        return Optional.ofNullable(kakaoMobilityResponse)
                .map(KakaoMobilityResponse::routes)
                .filter(routes -> !routes.isEmpty())
                .map(List::getFirst)
                .map(Route::sections)
                .map(sections -> sections.stream()
                        .filter(Objects::nonNull)
                        .flatMap(section -> Optional.ofNullable(section.roads()).stream()
                                .flatMap(Collection::stream)
                                .filter(Objects::nonNull)
                        )
                        .map(road -> {
                            double[] vertexes = road.vertexes();
                            List<Coordinate> coordinateList = new ArrayList<>();
                            if (vertexes != null) {
                                for (int i = 0; i < vertexes.length - 1; i += 2) {
                                    coordinateList.add(Coordinate.of(vertexes[i], vertexes[i + 1]));
                                }
                            }
                            return new DrivingRouteResponse(road.name(), coordinateList);
                        })
                        .toList()
                )
                .orElse(null);
    }
}
