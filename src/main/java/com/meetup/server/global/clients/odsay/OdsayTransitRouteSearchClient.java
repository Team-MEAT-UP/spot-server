package com.meetup.server.global.clients.odsay;

import com.meetup.server.global.clients.exception.ClientErrorType;
import com.meetup.server.global.clients.exception.ClientException;
import com.meetup.server.global.clients.ratelimit.LimitRequestPerDay;
import com.meetup.server.global.support.error.discord.DiscordAlarmSender;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OdsayTransitRouteSearchClient {

    private final RestClient odsayRestClient;
    private final OdsayProperties odsayProperties;
    private final DiscordAlarmSender discordAlarmSender;

    @LimitRequestPerDay(
            key = "odsay-transit",
            count = 1000
    )
    @Retry(name = "routeApi", fallbackMethod = "retryFallback")
    @CircuitBreaker(name = "routeApi", fallbackMethod = "circuitBreakerFallback")
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

        return odsayRestClient
                .get()
                .uri(uri)
                .retrieve()
                .body(OdsayTransitRouteSearchResponse.class);
    }

    private OdsayTransitRouteSearchResponse retryFallback(Throwable t) {
        log.warn("[Failed Retry] Fallback method executed. Reason: {}", t.getMessage());
        discordAlarmSender.sendErrorAlert(new ClientException(ClientErrorType.ODSAY_SERVICE_UNAVAILABLE));
        throw new ClientException(ClientErrorType.ODSAY_SERVICE_UNAVAILABLE);
    }

    private OdsayTransitRouteSearchResponse circuitBreakerFallback(Throwable t) {
        log.warn("[CircuitBreaker: OPEN] Fallback method executed. Reason: {}", t.getMessage());
        discordAlarmSender.sendErrorAlert(new ClientException(ClientErrorType.ODSAY_SERVICE_UNAVAILABLE));
        throw new ClientException(ClientErrorType.ODSAY_SERVICE_UNAVAILABLE);
    }
}
