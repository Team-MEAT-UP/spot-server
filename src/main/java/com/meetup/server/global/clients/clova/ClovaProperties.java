package com.meetup.server.global.clients.clova;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clova")
public record ClovaProperties(
        String studioApiKey,
        String requestId,
        String baseUrl,
        String basePath
) {
}
