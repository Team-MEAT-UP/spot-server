package com.meetup.server.event.infrastructure.cache;

import com.meetup.server.event.domain.RouteCache;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InMemoryRouteCache implements RouteCache {

    private static final String ROUTE_DETAILS_CACHE = "routeDetails";

    private final CacheManager cacheManager;

    @Override
    public Optional<MeetingPointRouteGroups> getByEventId(UUID eventId) {
        return Optional.ofNullable(getCache())
                .map(cache -> cache.get(eventId, MeetingPointRouteGroups.class));
    }

    @Override
    public void save(UUID eventId, MeetingPointRouteGroups routeGroups) {
        Cache cache = getCache();
        if (cache != null) {
            cache.put(eventId, routeGroups);
        }
    }

    @Override
    public void delete(UUID eventId) {
        Cache cache = getCache();
        if (cache != null) {
            cache.evict(eventId);
        }
    }

    private Cache getCache() {
        return cacheManager.getCache(ROUTE_DETAILS_CACHE);
    }
}
