package com.meetup.server.event.implement.route;

import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.response.route.MeetingPointResult;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.route.RouteResponse;
import com.meetup.server.event.implement.EventProcessor;
import com.meetup.server.event.infrastructure.redis.CachedRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class RouteProcessor {

    private final RouteAssembler routeAssembler;
    private final CachedRouteRepository cachedRouteRepository;
    private final EventProcessor eventProcessor;

    public List<MeetingPointRouteGroup> buildAndSaveRouteGroups(UUID eventId, List<MeetingPointResult> meetingPointResults) {
        List<MeetingPointRouteGroup> meetingPointRouteGroups = meetingPointResults.stream()
                .map(resultResponse -> routeAssembler.assemble(resultResponse.startPoints(), resultResponse.subway()))
                .toList();

        eventProcessor.saveRoute(eventId, new com.meetup.server.event.domain.value.MeetingPointRouteGroups(meetingPointRouteGroups));
        cachedRouteRepository.save(eventId, new MeetingPointRouteGroups(meetingPointRouteGroups));

        return meetingPointRouteGroups;
    }

    public void prioritizeMyRoute(Long userId, UUID guestId, List<RouteResponse> routeList) {
        Predicate<RouteResponse> isOwnedByUserOrGuest = (userId != null)
                ? route -> userId.equals(route.getUserId())
                : route -> guestId != null && guestId.equals(route.getGuestId());

        routeList.stream()
                .filter(isOwnedByUserOrGuest)
                .findFirst()
                .ifPresent(route -> {
                    route.updateIsMe(true);
                    routeList.remove(route);
                    routeList.addFirst(route);
                });
    }
}
