package com.meetup.server.global.clients.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.mtls")
public record TossMtlsProperties(
        String keyStoreBase64,
        String keyStorePassword,
        String keyStoreType
) {
}
