package com.meetup.server.global.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

@Slf4j
public class LoggingCache implements Cache {

    private final Cache delegate;

    public LoggingCache(Cache cache) {
        this.delegate = cache;
    }

    @Override
    public String getName() {
        log.info("[Redis] Cache Name: {}", delegate.getName());
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        log.info("[Redis] Accessing Native Cache instance");
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper valueWrapper = delegate.get(key);
        if (valueWrapper != null) {
            log.info("[Redis] Cache Hit AND Key: {}", key);
        } else {
            log.info("[Redis] Cache Miss AND Key: {}", key);
        }
        return valueWrapper;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T value = delegate.get(key, type);
        if (value != null) {
            log.info("[Redis] Cache Hit AND Key: {} with type: {}", key, type.getName());
        } else {
            log.info("[Redis] Cache Miss AND Key: {} with type: {}", key, type.getName());
        }
        return value;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        try {
            T value = delegate.get(key, valueLoader);
            if (value != null) {
                log.info("[Redis] Cache Hit AND Key: {}", key);
            } else {
                log.info("[Redis] Cache Miss AND Key: {}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("[Redis] Error occurred while loading cache for Key: {}", key, e);
            throw new RuntimeException("[Redis] Error occurred while loading cache", e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        log.info("[Redis] Putting Cache Key: {} with Value: {}", key, value);
        delegate.put(key, value);
    }

    @Override
    public void evict(Object key) {
        log.info("[Redis] Evicting Cache Key: {}", key);
        delegate.evict(key);
    }

    @Override
    public void clear() {
        log.info("[Redis] Clearing all cache entries");
        delegate.clear();
    }
}
