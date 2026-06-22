package com.meetup.server.auth.dto.response;

public record TossLoginMeApiResponse(
        String resultType,
        TossLoginMeResponse success,
        TossErrorResponse error
) {
}
