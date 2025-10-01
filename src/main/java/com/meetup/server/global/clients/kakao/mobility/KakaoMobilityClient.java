package com.meetup.server.global.clients.kakao.mobility;

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

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMobilityClient {

    private final RestClient kakaoMobilityRestClient;
    private final DiscordAlarmSender discordAlarmSender;

    @LimitRequestPerDay(
            key = "kakao-mobility",
            count = 10000
    )
    @Retry(name = "routeApi", fallbackMethod = "retryFallback")
    @CircuitBreaker(name = "routeApi", fallbackMethod = "circuitBreakerFallback")
    public KakaoMobilityResponse sendRequest(KakaoMobilityRequest request) {
        return kakaoMobilityRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("origin", request.origin())
                        .queryParam("destination", request.destination())
                        .queryParamIfPresent("waypoints", Optional.ofNullable(request.waypoints()))
                        .queryParamIfPresent("priority", Optional.ofNullable(request.priority()))
                        .queryParamIfPresent("avoid", Optional.ofNullable(request.avoid()))
                        .queryParamIfPresent("roadevent", Optional.ofNullable(request.roadEvent()))
                        .queryParamIfPresent("alternatives", Optional.ofNullable(request.alternatives()))
                        .queryParamIfPresent("road_details", Optional.ofNullable(request.roadDetails()))
                        .queryParamIfPresent("car_type", Optional.ofNullable(request.carType()))
                        .queryParamIfPresent("car_fuel", Optional.ofNullable(request.carFuel()))
                        .queryParamIfPresent("car_hipass", Optional.ofNullable(request.carHiPass()))
                        .queryParamIfPresent("summary", Optional.ofNullable(request.summary()))
                        .build())
                .retrieve()
                .body(KakaoMobilityResponse.class);
    }

    private KakaoMobilityResponse retryFallback(Throwable t) {
        log.warn("[Failed Retry] Fallback method executed. Reason: {}", t.getMessage());
        discordAlarmSender.sendErrorAlert(new ClientException(ClientErrorType.KAKAO_MOBILITY_SERVICE_UNAVAILABLE));
        throw new ClientException(ClientErrorType.KAKAO_MOBILITY_SERVICE_UNAVAILABLE);
    }

    private KakaoMobilityResponse circuitBreakerFallback(Throwable t) {
        log.warn("[CircuitBreaker: OPEN] Fallback method executed. Reason: {}", t.getMessage());
        discordAlarmSender.sendErrorAlert(new ClientException(ClientErrorType.KAKAO_MOBILITY_SERVICE_UNAVAILABLE));
        throw new ClientException(ClientErrorType.KAKAO_MOBILITY_SERVICE_UNAVAILABLE);
    }
}
