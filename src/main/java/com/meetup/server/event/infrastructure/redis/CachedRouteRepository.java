package com.meetup.server.event.infrastructure.redis;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.response.route.MeetingPointRouteGroup;
import com.meetup.server.event.implement.EventReader;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CachedRouteRepository {

    private final CacheManager cacheManager;
    private final EventReader eventReader;

    public Optional<MeetingPointRouteGroups> findByEventId(UUID eventId) {
        return Optional.ofNullable(getFromCache(eventId))
                .or(() -> fallback(eventId));
    }

    private MeetingPointRouteGroups getFromCache(UUID eventId) {
        return Optional.ofNullable(cacheManager.getCache("routeDetails"))
                .map(cache -> cache.get(eventId))
                .map(Cache.ValueWrapper::get)
                .filter(MeetingPointRouteGroups.class::isInstance)
                .map(MeetingPointRouteGroups.class::cast)
                .orElse(null);
    }

    private Optional<MeetingPointRouteGroups> fallback(UUID eventId) {
        Event event = eventReader.read(eventId);
        if (event.getRoute() == null) {
            return Optional.empty();
        }

        List<MeetingPointRouteGroup> list = event.getRoute().meetingPointRouteGroups()
                .stream()
                .map(meetingPointRouteGroup -> new MeetingPointRouteGroup(
                        meetingPointRouteGroup.subwayId(),
                        meetingPointRouteGroup.averageTime(),
                        meetingPointRouteGroup.meetingPoint(),
                        meetingPointRouteGroup.routeResponse(),
                        meetingPointRouteGroup.parkingLot()
                ))
                .toList();

        MeetingPointRouteGroups meetingPointRouteGroups = new MeetingPointRouteGroups(list);
        save(eventId, meetingPointRouteGroups);

        return Optional.of(meetingPointRouteGroups);
    }

    public void save(UUID eventId, MeetingPointRouteGroups cached) {
        Cache cache = cacheManager.getCache("routeDetails");
        if (cache != null) {
            cache.put(eventId, cached);
        }
    }

    public void delete(UUID eventId) {
        Cache cache = cacheManager.getCache("routeDetails");
        if (cache != null) {
            cache.evict(eventId);
        }
    }
}
