package com.meetup.server.global.clients.ratelimit;

public interface RateLimiter {

    void tryApiCall(LimitRequestPerDay limitRequestPerDay);
}
