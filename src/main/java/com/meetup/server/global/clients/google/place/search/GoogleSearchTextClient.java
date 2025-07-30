package com.meetup.server.global.clients.google.place.search;

import com.meetup.server.global.clients.google.place.GoogleFieldMask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GoogleSearchTextClient {

    private final WebClient googlePlaceWebClient;

    public GoogleSearchTextResponse sendRequest(GoogleSearchTextRequest request, GoogleFieldMask googleFieldMask) {
        return googlePlaceWebClient.post()
                .uri("/places:searchText")
                .header("X-Goog-FieldMask", googleFieldMask.getMask())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GoogleSearchTextResponse.class)
                .block();
    }
}
