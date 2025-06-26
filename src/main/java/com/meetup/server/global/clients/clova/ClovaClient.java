package com.meetup.server.global.clients.clova;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class ClovaClient {

    private final ClovaProperties clovaProperties;
    private final WebClient clovaWebClient;

    public ClovaResponse sendRequest(ClovaRequest request) {
        return clovaWebClient
                .post()
                .uri(clovaProperties.basePath())
                .header("Authorization", "Bearer " + clovaProperties.studioApiKey())
                .header("X-NCP-CLOVASTUDIO-REQUEST-ID", clovaProperties.requestId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClovaResponse.class)
                .block();
    }
}
