package com.meetup.server.auth.dto.response;

public record TossLoginMeResponse(
        String userKey,
        String scope
) {
}
