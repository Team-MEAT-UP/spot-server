package com.meetup.server.global.clients.clova;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
public record ClovaResponse(
        Status status,
        Result result
) {
    public record Status(
            String code,
            String message
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            Message message
    ) {
        public record Message(
                String role,
                String content
        ) {
        }
    }
}
