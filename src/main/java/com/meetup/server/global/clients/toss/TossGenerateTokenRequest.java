package com.meetup.server.global.clients.toss;

public record TossGenerateTokenRequest(
        String authorizationCode,
        String referrer
) {
}
