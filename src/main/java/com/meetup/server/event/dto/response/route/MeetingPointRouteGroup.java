package com.meetup.server.event.dto.response.route;

import com.meetup.server.parkinglot.dto.response.ParkingLotResponse;
import com.meetup.server.parkinglot.infrastructure.jpa.projection.ClosestParkingLot;
import com.meetup.server.subway.domain.Subway;
import lombok.Builder;

import java.util.List;

@Builder
public record MeetingPointRouteGroup(
        int subwayId,
        int averageTime,
        MeetingPoint meetingPoint,
        List<RouteResponse> routeResponse,
        ParkingLotResponse parkingLot
) {
    public static MeetingPointRouteGroup of(List<RouteResponse> routeResponse, Subway subway, ClosestParkingLot closestParkingLot) {
        return MeetingPointRouteGroup.builder()
                .subwayId(subway.getSubwayId())
                .averageTime(calculateAverageTime(routeResponse))
                .meetingPoint(MeetingPoint.from(subway))
                .routeResponse(routeResponse)
                .parkingLot(ParkingLotResponse.from(closestParkingLot))
                .build();
    }

    private static int calculateAverageTime(List<RouteResponse> routeResponse) {
        return (int) routeResponse.stream()
                .mapToInt(RouteResponse::getTotalTime)
                .filter(time -> time > 0)
                .average()
                .orElse(0);
    }
}
