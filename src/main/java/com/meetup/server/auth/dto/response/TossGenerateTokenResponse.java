package com.meetup.server.auth.dto.response;

public record TossGenerateTokenResponse(
        String resultType,
        TossGenerateTokenSuccess success,
        TossErrorResponse error
) {
}
