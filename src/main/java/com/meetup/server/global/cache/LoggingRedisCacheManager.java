package com.meetup.server.global.cache;

import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

public class LoggingRedisCacheManager extends RedisCacheManager {

    public LoggingRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfig) {
        super(cacheWriter, defaultCacheConfig);
    }

    @Override
    protected Cache decorateCache(Cache cache) {
        return new LoggingCache(cache);
    }
}
