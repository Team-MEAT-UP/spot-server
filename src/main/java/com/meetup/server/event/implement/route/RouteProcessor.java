package com.meetup.server.event.implement.route;

import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.response.route.MeetingPointResult;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.dto.response.route.RouteResponse;
import com.meetup.server.event.exception.EventErrorType;
import com.meetup.server.event.exception.EventException;
import com.meetup.server.event.implement.EventProcessor;
import com.meetup.server.event.infrastructure.redis.CachedRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteProcessor {

    private final RouteAssembler routeAssembler;
    private final CachedRouteRepository cachedRouteRepository;
    private final EventProcessor eventProcessor;

    public MeetingPointRouteGroup buildRouteGroup(MeetingPointResult meetingPointResult) {
        try {
            return routeAssembler.assemble(meetingPointResult.startPoints(), meetingPointResult.subway()).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventException(EventErrorType.ROUTE_FETCH_FAILED);
        } catch (Exception e) {
            log.warn("[RouteProcessor] Failed building MeetingPointRouteGroup", e);
            throw new EventException(EventErrorType.ROUTE_FETCH_FAILED);
        }
    }

    public void saveRouteGroups(UUID eventId, MeetingPointRouteGroups groupedRoutes) {
        eventProcessor.saveRoute(eventId, groupedRoutes);
        cachedRouteRepository.save(eventId, groupedRoutes);
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
        cachedRouteRepository.delete(eventId);
    }
}
