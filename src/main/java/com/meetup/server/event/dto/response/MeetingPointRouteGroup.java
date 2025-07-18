package com.meetup.server.event.dto.response;

import com.meetup.server.parkinglot.persistence.projection.ClosestParkingLot;
import com.meetup.server.subway.domain.Subway;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingPointRouteGroup {

    private int subwayId;
    private int averageTime;
    private MeetingPoint meetingPoint;
    private List<RouteResponse> routeResponse;
    private ParkingLotResponse parkingLot;

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
        return routeResponse.stream()
                .mapToInt(RouteResponse::getTotalTime)
                .sum() / routeResponse.size();
    }
}
