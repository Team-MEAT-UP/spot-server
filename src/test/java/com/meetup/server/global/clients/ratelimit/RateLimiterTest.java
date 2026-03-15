package com.meetup.server.global.clients.ratelimit;

import com.meetup.server.global.util.TimeUtil;
import com.meetup.server.support.IntegrationTestContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class RateLimiterTest extends IntegrationTestContainer {

    @Autowired
    ApiCallLimitRepository apiCallLimitRepository;

    @Autowired
    RateLimiter rateLimiter;

    @BeforeEach
    void resetKeys() {
        apiCallLimitRepository.deleteAll();
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

        LocalDate today = ZonedDateTime.now(TimeUtil.KST_ZONE_ID).toLocalDate();
        int count = apiCallLimitRepository.findByApiNameAndCallDate("test", today)
                .map(ApiCallLimit::getCount)
                .orElse(0);
        Assertions.assertEquals(5, count);
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

        LocalDate today = ZonedDateTime.now(TimeUtil.KST_ZONE_ID).toLocalDate();
        int count = apiCallLimitRepository.findByApiNameAndCallDate("test", today)
                .map(ApiCallLimit::getCount)
                .orElse(0);
        Assertions.assertEquals(10, count);
    }

}
