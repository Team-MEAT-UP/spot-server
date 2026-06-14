package com.meetup.server.auth.dto.response;

public record TossLoginResponse(
        String accessToken,
        String refreshToken
) {
}
