package com.meetup.server.event.implement.route;

import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.route.RouteResponse;
import com.meetup.server.event.exception.EventErrorType;
import com.meetup.server.event.exception.EventException;
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
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteAssembler {

    private static final int MAX_ATTEMPTS = 2;
    private static final int RETRY_DELAY_MS = 100;

    private final ExecutorService routeExecutor;
    private final ParkingLotFinder parkingLotFinder;
    private final RouteFetcher routeFetcher;

    public CompletableFuture<MeetingPointRouteGroup> assemble(List<StartPoint> startPoints, Subway subway) {
        List<CompletableFuture<RouteResponse>> routeFutures = startPoints.stream()
                .map(startPoint -> CompletableFuture.supplyAsync(() -> fetchWithRetry(startPoint, subway), routeExecutor))
                .toList();

        CompletableFuture<List<RouteResponse>> allRoutesFuture = CompletableFuture.allOf(routeFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> routeFutures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));

        CompletableFuture<ClosestParkingLot> closestParkingLotFuture =
                CompletableFuture.supplyAsync(() -> parkingLotFinder.findClosestParkingLot(subway.getPoint()), routeExecutor);

        return allRoutesFuture.thenCombine(closestParkingLotFuture, (routes, closestParkingLot) -> {
            if (routes.isEmpty()) {
                log.warn("[RouteAssembler] No routes found for subway: {}", subway.getName());
                return null;
            }
            return MeetingPointRouteGroup.of(routes, subway, closestParkingLot);
        });
    }

    private RouteResponse fetchWithRetry(StartPoint startPoint, Subway subway) {
        RouteResponse route = null;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            route = routeFetcher.fetch(startPoint, subway);

            if (isValid(route)) {
                return route;
            }

            if (attempt < MAX_ATTEMPTS - 1) {
                log.warn("[RouteAssembler] Route fetch failed for {}. Retrying... (Attempt {}/{})",
                        startPoint.getName(), attempt + 1, MAX_ATTEMPTS);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new EventException(EventErrorType.ROUTE_FETCH_FAILED);
                }
            }
        }

        log.warn("[RouteAssembler] All retry attempts failed. StartPoint: {}, Subway: {}", startPoint.getName(), subway.getName());
        return route;
    }

    private boolean isValid(RouteResponse route) {
        if (route == null) return false;
        return (route.getIsTransit() && route.getTransitRoute() != null) ||
                (!route.getIsTransit() && route.getDrivingRoute() != null);
    }
}
