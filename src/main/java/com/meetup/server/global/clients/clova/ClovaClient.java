package com.meetup.server.global.clients.clova;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ClovaClient {

    private final ClovaProperties clovaProperties;
    private final RestClient clovaRestClient;

    public ClovaResponse sendRequest(ClovaRequest request) {
        return clovaRestClient
                .post()
                .uri(clovaProperties.basePath())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ClovaResponse.class);
    }
}
