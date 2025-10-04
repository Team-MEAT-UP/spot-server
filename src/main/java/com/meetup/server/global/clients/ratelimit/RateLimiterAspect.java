package com.meetup.server.global.clients.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class RateLimiterAspect {

    private final RateLimiter rateLimiter;

    public RateLimiterAspect(@Qualifier("redisRateLimiter") RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Around("@annotation(com.meetup.server.global.clients.ratelimit.LimitRequestPerDay)")
    public Object applyRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        LimitRequestPerDay limitRequestPerDay = getLimitRequestPerDayAnnotationFromMethod(joinPoint);
        rateLimiter.tryApiCall(limitRequestPerDay);
        return joinPoint.proceed();
    }

    private LimitRequestPerDay getLimitRequestPerDayAnnotationFromMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(LimitRequestPerDay.class);
    }
}
