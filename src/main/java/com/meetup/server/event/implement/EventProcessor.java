package com.meetup.server.event.implement;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.response.MeetingPointRouteGroup;
import com.meetup.server.event.dto.request.EventRequest;
import com.meetup.server.event.dto.response.RouteResponse;
import com.meetup.server.event.persistence.EventRepository;
import com.meetup.server.startpoint.domain.StartPoint;
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

    public Event save(EventRequest eventRequest) {
        Event event = Event.builder()
                .eventName(eventRequest.eventName())
                .eventDateTime(eventRequest.toDateTime())
                .build();
        return eventRepository.save(event);
    }

    public void updateTransitForStartPoint(List<MeetingPointRouteGroup> meetingPointRouteGroup, UUID startPointId, boolean isTransit) {
        for (MeetingPointRouteGroup routeResponse : meetingPointRouteGroup) {
            routeResponse.updateIsTransitForStartPoint(startPointId, isTransit);
        }

        StartPoint startPoint = startPointReader.read(startPointId);
        startPoint.updateIsTransit(isTransit);
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
