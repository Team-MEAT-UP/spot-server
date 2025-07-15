package com.meetup.server.event.implement;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.response.RouteResponse;
import com.meetup.server.event.dto.response.RouteResponseList;
import com.meetup.server.event.persistence.EventRepository;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.implement.StartPointProcessor;
import com.meetup.server.startpoint.implement.StartPointReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class EventProcessor {

    private final EventRepository eventRepository;
    private final StartPointReader startPointReader;
    private final StartPointProcessor startPointProcessor;

    public Event save() {
        Event event = Event.builder().build();
        return eventRepository.save(event);
    }

    public void updateTransitForStartPoint(RouteResponseList routeResponseList, UUID startPointId, boolean isTransit) {
        List<RouteResponse> routes = routeResponseList.getRouteResponse();

        routes.stream()
                .filter(route -> startPointId.equals(route.getId()))
                .findFirst()
                .ifPresent(route -> {
                    route.updateIsTransit(isTransit);
                    StartPoint startPoint = startPointReader.read(startPointId);
                    startPointProcessor.updateTransit(startPoint, isTransit);
                });

        routeResponseList.updateRouteResponse(routes);
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
