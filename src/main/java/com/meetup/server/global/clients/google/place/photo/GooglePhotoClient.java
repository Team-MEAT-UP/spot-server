package com.meetup.server.global.clients.google.place.photo;

import com.meetup.server.global.clients.google.place.GooglePlaceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GooglePhotoClient {

    private final RestClient googlePlaceRestClient;
    private final GooglePlaceProperties googlePlaceProperties;

    public GooglePhotoResponse sendRequest(GooglePhotoRequest request) {
        return googlePlaceRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + request.name() + "/media")
                        .queryParam("key", googlePlaceProperties.secretKey())
                        .queryParamIfPresent("maxHeightPx", Optional.ofNullable(request.maxHeightPx()))
                        .queryParamIfPresent("maxWidthPx", Optional.ofNullable(request.maxWidthPx()))
                        .build()
                )
                .retrieve()
                .body(GooglePhotoResponse.class);
    }
}
