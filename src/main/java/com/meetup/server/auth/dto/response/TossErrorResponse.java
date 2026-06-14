package com.meetup.server.auth.dto.response;

public record TossErrorResponse(
        String errorCode,
        String reason
) {
}
