package com.meetup.server.global.clients.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.server.auth.dto.response.TossGenerateTokenResponse;
import com.meetup.server.auth.dto.response.TossLoginMeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class TossAuthClient {

    @Qualifier("tossRestClient")
    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossGenerateTokenResponse generateToken(TossGenerateTokenRequest request) {
        return tossRestClient
                .post()
                .uri("/api-partner/v1/apps-in-toss/user/oauth2/generate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange((clientRequest, clientResponse) ->
                        readBody(clientResponse.getBody(), TossGenerateTokenResponse.class)
                );
    }

    public TossLoginMeResponse loginMe(String accessToken) {
        return tossRestClient
                .get()
                .uri("/api-partner/v1/apps-in-toss/user/oauth2/login-me")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .exchange((clientRequest, clientResponse) ->
                        readBody(clientResponse.getBody(), TossLoginMeResponse.class)
                );
    }

    private <T> T readBody(InputStream body, Class<T> responseType) throws IOException {
        return objectMapper.readValue(body, responseType);
    }
}
