package com.meetup.server.global.support.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secretKey,
        String issuer,
        Long accessTokenExpiration,
        Long refreshTokenExpiration
) {
}
