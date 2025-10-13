package com.meetup.server.global.clients.google.place.search;

import com.meetup.server.global.clients.google.place.GoogleFieldMask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GoogleSearchTextClient {

    private final RestClient googlePlaceRestClient;

    public GoogleSearchTextResponse sendRequest(GoogleSearchTextRequest request, GoogleFieldMask googleFieldMask) {
        return googlePlaceRestClient.post()
                .uri("/places:searchText")
                .header("X-Goog-FieldMask", googleFieldMask.getMask())
                .body(request)
                .retrieve()
                .body(GoogleSearchTextResponse.class);
    }
}
