package com.meetup.server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisScriptConfig {

    @Bean
    public RedisScript<Long> redisRateLimitScript() {
        Resource redisRateLimitScript = new ClassPathResource("scripts/rate_limit_script.lua");
        return RedisScript.of(redisRateLimitScript, Long.class);
    }

    @Bean
    public RedisScript<Long> refreshTokenRotateScript() {
        Resource refreshTokenRotateScript = new ClassPathResource("scripts/refresh_token_rotate_script.lua");
        return RedisScript.of(refreshTokenRotateScript, Long.class);
    }
}
