package com.meetup.server.global.clients.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.api")
public record TossApiProperties(
        String baseUrl
) {
}
