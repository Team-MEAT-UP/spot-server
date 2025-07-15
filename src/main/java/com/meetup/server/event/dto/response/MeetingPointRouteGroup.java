package com.meetup.server.event.dto.response;

import com.meetup.server.parkinglot.persistence.projection.ClosestParkingLot;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.util.UsernameExtractor;
import com.meetup.server.subway.domain.Subway;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingPointRouteGroup {

    private int subwayId;
    private String eventMaker;
    private int peopleCount;
    private int averageTime;
    private MeetingPoint meetingPoint;
    private List<RouteResponse> routeResponse;
    private ParkingLotResponse parkingLot;

    public static MeetingPointRouteGroup of(StartPoint startPoint, List<RouteResponse> routeResponse, Subway subway, ClosestParkingLot closestParkingLot) {
        return MeetingPointRouteGroup.builder()
                .subwayId(subway.getSubwayId())
                .eventMaker(UsernameExtractor.extractDisplayName(startPoint))
                .averageTime(calculateAverageTime(routeResponse))
                .peopleCount(routeResponse.size())
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

    public void updateIsTransitForStartPoint(UUID startPointId, boolean isTransit) {
        this.routeResponse.stream()
                .filter(route -> startPointId.equals(route.getId()))
                .findFirst()
                .ifPresent(route -> route.updateIsTransit(isTransit));
        this.averageTime = calculateAverageTime(this.routeResponse);
    }
}
