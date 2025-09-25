package com.meetup.server.event.implement.route;

import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.response.route.MeetingPointResult;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.route.RouteResponse;
import com.meetup.server.event.implement.EventProcessor;
import com.meetup.server.event.infrastructure.redis.CachedRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RouteProcessor {

    private final RouteAssembler routeAssembler;
    private final CachedRouteRepository cachedRouteRepository;
    private final EventProcessor eventProcessor;

    public List<MeetingPointRouteGroup> buildRouteGroups(List<MeetingPointResult> meetingPointResults) {
        List<MeetingPointRouteGroup> routeGroups;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<MeetingPointRouteGroup>> routeGroupFutures = meetingPointResults.stream()
                    .map(meetingPointResult -> executor.submit(
                            () -> routeAssembler.assemble(meetingPointResult.startPoints(), meetingPointResult.subway())
                    ))
                    .toList();

            routeGroups = routeGroupFutures.stream()
                    .flatMap(routeFuture -> {
                        try {
                            return Optional.ofNullable(routeFuture.get()).stream();
                        } catch (Exception e) {
                            return Stream.empty();
                        }
                    })
                    .collect(Collectors.toList());
        }
        return routeGroups;
    }

    public void saveRouteGroups(UUID eventId, List<MeetingPointRouteGroup> routeGroups) {
        MeetingPointRouteGroups groupedRoutes = new MeetingPointRouteGroups(routeGroups);
        eventProcessor.saveRoute(eventId, groupedRoutes);
        cachedRouteRepository.save(eventId, groupedRoutes);
    }

    public void prioritizeMyRoute(Long userId, UUID guestId, List<RouteResponse> routeList) {
        routeList.forEach(route -> {
            boolean isMine = (userId != null && userId.equals(route.getUserId()))
                    || (guestId != null && guestId.equals(route.getGuestId()));
            route.updateIsMe(isMine);
        });

        routeList.sort(Comparator.comparing((RouteResponse route) -> route.getTotalTime() == 0)
                .thenComparing(RouteResponse::getIsMe, Comparator.reverseOrder())
        );
    }

    public void deleteCache(UUID eventId) {
        cachedRouteRepository.delete(eventId);
    }
}
