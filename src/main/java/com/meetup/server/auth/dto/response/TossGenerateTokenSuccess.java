package com.meetup.server.auth.dto.response;

public record TossGenerateTokenSuccess(
        String tokenType,
        String accessToken,
        String refreshToken,
        String expiresIn,
        String scope
) {
}
