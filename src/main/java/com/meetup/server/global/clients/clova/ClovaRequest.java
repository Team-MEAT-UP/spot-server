package com.meetup.server.global.clients.clova;

import lombok.Builder;

import java.util.List;

@Builder
public record ClovaRequest(
        List<Message> messages,
        double topP,
        int topK,
        int maxTokens,
        double temperature,
        double repeatPenalty,
        List<String> stopBefore,
        boolean includeAiFilters,
        int seed

) {
    @Builder
    public record Message(
            String role,
            String content
    ) {
    }
}
