package com.meetup.server.event.infrastructure.redis;

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

    public Optional<MeetingPointRouteGroupsCache> findByEventId(UUID eventId) {
        return Optional.ofNullable(getFromCache(eventId))
                .or(() -> fallback(eventId));
    }

    private MeetingPointRouteGroupsCache getFromCache(UUID eventId) {
        Cache cache = cacheManager.getCache("routeDetails");
        if (cache == null) return null;

        Cache.ValueWrapper wrapper = cache.get(eventId);
        if (wrapper == null) return null;

        Object cachedValue = wrapper.get();
        if (cachedValue instanceof MeetingPointRouteGroupsCache cached) {
            return cached;
        }
        return null;
    }

    private Optional<MeetingPointRouteGroupsCache> fallback(UUID eventId) {
        // TODO: DB 조회 로직 구현 및 캐시 저장
        return Optional.empty();
    }

    public void save(UUID eventId, MeetingPointRouteGroupsCache cached) {
        Cache cache = cacheManager.getCache("routeDetails");
        if (cache != null) {
            cache.put(eventId, cached);
        }
    }
}
