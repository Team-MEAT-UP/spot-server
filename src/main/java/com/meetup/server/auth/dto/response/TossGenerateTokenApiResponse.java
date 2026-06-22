package com.meetup.server.auth.dto.response;

public record TossGenerateTokenApiResponse(
        String resultType,
        TossGenerateTokenResponse success,
        TossErrorResponse error
) {
}
