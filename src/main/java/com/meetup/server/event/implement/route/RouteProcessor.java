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
import java.util.UUID;

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

        eventProcessor.saveRoute(eventId, new MeetingPointRouteGroups(meetingPointRouteGroups));
        cachedRouteRepository.save(eventId, new MeetingPointRouteGroups(meetingPointRouteGroups));

        return meetingPointRouteGroups;
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
