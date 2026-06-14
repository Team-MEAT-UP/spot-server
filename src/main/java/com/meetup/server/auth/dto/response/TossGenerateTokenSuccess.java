package com.meetup.server.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossGenerateTokenSuccess(
        String tokenType,
        String accessToken,
        String refreshToken,
        String expiresIn,
        String scope
) {
}
