package com.meetup.server.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossErrorResponse(
        String errorCode,
        String reason
) {
    @JsonCreator
    public TossErrorResponse(JsonNode error) {
        this(
                error.isTextual() ? error.asText() : error.path("errorCode").asText(null),
                error.isTextual() ? null : error.path("reason").asText(null)
        );
    }
}
