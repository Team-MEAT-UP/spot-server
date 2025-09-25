package com.meetup.server.event.implement.route;

import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.route.RouteResponse;
import com.meetup.server.parkinglot.implement.ParkingLotFinder;
import com.meetup.server.parkinglot.infrastructure.jpa.projection.ClosestParkingLot;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.subway.domain.Subway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteAssembler {

    private final ParkingLotFinder parkingLotFinder;
    private final RouteFetcher routeFetcher;

    public MeetingPointRouteGroup assemble(List<StartPoint> startPointList, Subway subway) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<RouteResponse>> routeFutures = startPointList.stream()
                    .map(startPoint -> executor.submit(() -> routeFetcher.fetch(startPoint, subway)))
                    .toList();
            Future<ClosestParkingLot> parkingLotFuture = executor.submit(() -> parkingLotFinder.findClosestParkingLot(subway.getPoint()));

            List<RouteResponse> routeList = getRoutesFromFutures(routeFutures);
            ClosestParkingLot closestParkingLot = getClosestParkingLotFromFutures(parkingLotFuture);

            return MeetingPointRouteGroup.of(routeList, subway, closestParkingLot);
        }
    }

    private List<RouteResponse> getRoutesFromFutures(List<Future<RouteResponse>> routeFutures) {
        return routeFutures.stream()
                .flatMap(routeFuture -> {
                    try {
                        return Optional.ofNullable(routeFuture.get()).stream();
                    } catch (Exception e) {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    private ClosestParkingLot getClosestParkingLotFromFutures(Future<ClosestParkingLot> parkingLotFuture) {
        try {
            return parkingLotFuture.get();
        } catch (Exception e) {
            return null;
        }
    }
}
