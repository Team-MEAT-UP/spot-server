package com.meetup.server.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossGenerateTokenResponse(
        String resultType,
        TossGenerateTokenSuccess success,
        TossErrorResponse error
) {
}
