package com.meetup.server.event.implement.route;

import com.meetup.server.event.domain.RouteCache;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.response.route.MeetingPointResult;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.route.RouteResponse;
import com.meetup.server.event.exception.EventErrorType;
import com.meetup.server.event.exception.EventException;
import com.meetup.server.event.implement.EventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteProcessor {

    private final RouteAssembler routeAssembler;
    private final EventProcessor eventProcessor;
    private final RouteCache routeCache;

    public List<MeetingPointRouteGroup> buildRouteGroups(List<MeetingPointResult> meetingPointResults) {
        List<CompletableFuture<MeetingPointRouteGroup>> routeGroupFutures = meetingPointResults.stream()
                .map(meetingPointResult -> routeAssembler.assemble(meetingPointResult.startPoints(), meetingPointResult.subway()))
                .toList();

        CompletableFuture<Void> allCompletedFuture = CompletableFuture.allOf(routeGroupFutures.toArray(new CompletableFuture[0]));
        try {
            List<MeetingPointRouteGroup> routeGroups = allCompletedFuture.thenApply(v -> routeGroupFutures.stream()
                            .map(routeGroupFuture -> {
                                try {
                                    return routeGroupFuture.get();
                                } catch (Exception e) {
                                    log.warn("[RouteProcessor] Failed building MeetingPointRouteGroup", e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()))
                    .get();

            if (routeGroups.isEmpty()) {
                throw new EventException(EventErrorType.ROUTE_FETCH_FAILED);
            }
            return routeGroups;
        } catch (Exception e) {
            throw new EventException(EventErrorType.ROUTE_FETCH_FAILED);
        }
    }

    public void saveRouteGroups(UUID eventId, List<MeetingPointRouteGroup> routeGroups) {
        MeetingPointRouteGroups groupedRoutes = new MeetingPointRouteGroups(routeGroups);
        eventProcessor.saveRoute(eventId, groupedRoutes);
        routeCache.save(eventId, groupedRoutes);
    }

    public void prioritizeMyRoute(Long userId, UUID guestId, List<RouteResponse> routes) {
        routes.forEach(route -> {
            boolean isMine = (userId != null && userId.equals(route.getUserId()))
                    || (guestId != null && guestId.equals(route.getGuestId()));
            route.updateIsMe(isMine);
        });

        routes.sort(Comparator.comparing((RouteResponse route) -> route.getTotalTime() == 0)
                .thenComparing(RouteResponse::getIsMe, Comparator.reverseOrder())
        );
    }

    public void deleteCache(UUID eventId) {
        routeCache.delete(eventId);
    }
}
