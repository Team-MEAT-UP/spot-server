package com.meetup.server.global.clients.odsay;

import com.meetup.server.global.clients.util.LimitRequestPerDay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OdsayTransitRouteSearchClient {

    private final RestClient restClient;
    private final OdsayProperties odsayProperties;

    @LimitRequestPerDay(
            key = "odsay-transit",
            count = 1000
    )
    public OdsayTransitRouteSearchResponse sendRequest(OdsayTransitRouteSearchRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(odsayProperties.baseUrl() + "/searchPubTransPathT")
                .queryParam("apiKey", odsayProperties.secretKey())
                .queryParam("SX", request.sx())
                .queryParam("SY", request.sy())
                .queryParam("EX", request.ex())
                .queryParam("EY", request.ey())
                .queryParamIfPresent("OPT", Optional.ofNullable(request.opt()))
                .queryParamIfPresent("SearchType", Optional.ofNullable(request.searchType()))
                .queryParamIfPresent("SearchPathType", Optional.ofNullable(request.searchPathType()))
                .build(true)
                .toUri();

        return restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(OdsayTransitRouteSearchResponse.class);
    }
}
