package com.meetup.server.subway.infrastructure.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seoul.open-api")
public record SeoulSubwayProperties(
        String key,
        String url
) {
}
