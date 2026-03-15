package com.meetup.server.global.clients.ratelimit;

import com.meetup.server.global.clients.exception.ClientErrorType;

public record RateLimitAlertEvent(
        ClientErrorType clientErrorType
) {
}
