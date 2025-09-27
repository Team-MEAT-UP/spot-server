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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteAssembler {

    private final ParkingLotFinder parkingLotFinder;
    private final RouteFetcher routeFetcher;

    public CompletableFuture<MeetingPointRouteGroup> assemble(List<StartPoint> startPoints, Subway subway) {
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<CompletableFuture<RouteResponse>> routeFutures = startPoints.stream()
                    .map(startPoint -> CompletableFuture.supplyAsync(() -> routeFetcher.fetch(startPoint, subway), executor))
                    .toList();

            CompletableFuture<List<RouteResponse>> allRoutesFuture = CompletableFuture.allOf(routeFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> routeFutures.stream()
                            .map(routeFuture -> {
                                try {
                                    return routeFuture.get();
                                } catch (Exception e) {
                                    log.warn("[RouteAssembler] Failed fetching route", e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));

            CompletableFuture<ClosestParkingLot> closestParkingLotFuture =
                    CompletableFuture.supplyAsync(() -> parkingLotFinder.findClosestParkingLot(subway.getPoint()), executor);

            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(allRoutesFuture, closestParkingLotFuture);
            return combinedFuture.thenApply(v -> {
                try {
                    List<RouteResponse> routes = allRoutesFuture.get();
                    ClosestParkingLot closestParkingLot = closestParkingLotFuture.get();

                    if (routes == null || routes.isEmpty()) {
                        log.warn("[RouteAssembler] No routes found for subway: {}", subway);
                        return null;
                    }
                    return MeetingPointRouteGroup.of(routes, subway, closestParkingLot);
                } catch (Exception e) {
                    log.warn("[RouteAssembler] Failed assembling MeetingPointRouteGroup", e);
                    return null;
                }
            });
        }
    }
}
