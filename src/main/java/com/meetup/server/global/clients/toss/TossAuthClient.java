package com.meetup.server.global.clients.toss;

import com.meetup.server.auth.dto.response.TossGenerateTokenResponse;
import com.meetup.server.auth.dto.response.TossLoginMeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TossAuthClient {

    private final RestClient tossRestClient;

    public TossGenerateTokenResponse generateToken(TossGenerateTokenRequest request) {
        return tossRestClient
                .post()
                .uri("/api-partner/v1/apps-in-toss/user/oauth2/generate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TossGenerateTokenResponse.class);
    }

    public TossLoginMeResponse loginMe(String accessToken) {
        return tossRestClient
                .get()
                .uri("/api-partner/v1/apps-in-toss/user/oauth2/login-me")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(TossLoginMeResponse.class);
    }
}
