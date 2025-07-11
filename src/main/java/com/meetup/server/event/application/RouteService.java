package com.meetup.server.event.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.response.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.RouteResponse;
import com.meetup.server.parkinglot.implement.ParkingLotFinder;
import com.meetup.server.parkinglot.persistence.projection.ClosestParkingLot;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.implement.StartPointReader;
import com.meetup.server.subway.domain.Subway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final ParkingLotFinder parkingLotFinder;
    private final RouteDetailService routeDetailService;
    private final StartPointReader startPointReader;

    public MeetingPointRouteGroup getAllRouteDetails(Event event, List<StartPoint> startPointList, Subway subway) {

        List<RouteResponse> routeList = startPointList.stream()
                .map(startPoint -> routeDetailService.fetchPerRouteDetails(
                        startPoint,
                        String.valueOf(startPoint.getLocation().getRoadLongitude()),
                        String.valueOf(startPoint.getLocation().getRoadLatitude()),
                        String.valueOf(subway.getLocation().getRoadLongitude()),
                        String.valueOf(subway.getLocation().getRoadLatitude())
                ))
                .collect(Collectors.toList());

        ClosestParkingLot closestParkingLot = parkingLotFinder.findClosestParkingLot(subway.getPoint());

        StartPoint earliestStartPoint = startPointReader.readEarliestByEventId(event.getEventId());
        return MeetingPointRouteGroup.of(earliestStartPoint, routeList, subway, closestParkingLot);
    }
}
