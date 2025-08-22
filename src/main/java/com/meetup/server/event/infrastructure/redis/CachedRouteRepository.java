package com.meetup.server.event.infrastructure.redis;

import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CachedRouteRepository {

    private final CacheManager cacheManager;

    public MeetingPointRouteGroups findByEventId(UUID eventId) {
        return Optional.ofNullable(cacheManager.getCache("routeDetails"))
                .map(cache -> cache.get(eventId))
                .map(Cache.ValueWrapper::get)
                .filter(MeetingPointRouteGroups.class::isInstance)
                .map(MeetingPointRouteGroups.class::cast)
                .orElse(null);
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
