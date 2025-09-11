package com.meetup.server.global.clients.util;

import com.meetup.server.support.IntegrationTestContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.annotation.Annotation;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class RateLimiterTest extends IntegrationTestContainer {

    @Autowired
    RedisTemplate<String, String> redisRateLimitTemplate;

    @Autowired
    RateLimiter rateLimiter;

    @BeforeEach
    void resetKeys() {
        redisRateLimitTemplate.delete("test");
    }

    @Test
    void 싱글스레드에서_최대요청수만큼만_허용한다() {
        // given
        LimitRequestPerDay annotation = new LimitRequestPerDay() {
            @Override
            public String key() {
                return "test";
            }

            @Override
            public int count() {
                return 5;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return LimitRequestPerDay.class;
            }
        };

        for (int i = 0; i < 10; i++) {
            rateLimiter.tryApiCall(annotation);
        }

        Long count = Long.valueOf(redisRateLimitTemplate.opsForValue().get("test"));
        Assertions.assertEquals(5L, count);
    }

    @Test
    void 동시_요청_상황에서도_요청제한을_보장한다() throws InterruptedException {
        LimitRequestPerDay annotation = new LimitRequestPerDay() {
            @Override
            public String key() {
                return "test";
            }

            @Override
            public int count() {
                return 10;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return LimitRequestPerDay.class;
            }
        };

        int totalThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(totalThreads);

        for (int i = 0; i < totalThreads; i++) {
            executorService.submit(() -> {
                rateLimiter.tryApiCall(annotation);
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        Long count = Long.valueOf(redisRateLimitTemplate.opsForValue().get("test"));
        Assertions.assertEquals(10L, count);
    }

}
