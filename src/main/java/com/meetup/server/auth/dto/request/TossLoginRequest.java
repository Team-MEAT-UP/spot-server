package com.meetup.server.auth.dto.request;

public record TossLoginRequest(
        String authorizationCode,
        String referrer
) {
}
