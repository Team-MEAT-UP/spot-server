package com.meetup.server.global.config;

import com.meetup.server.global.clients.clova.ClovaProperties;
import com.meetup.server.global.clients.google.place.GooglePlaceProperties;
import com.meetup.server.global.clients.kakao.local.KakaoLocalProperties;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final KakaoLocalProperties kakaoLocalProperties;
    private final KakaoMobilityProperties kakaoMobilityProperties;
    private final GooglePlaceProperties googlePlaceProperties;
    private final ClovaProperties clovaProperties;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }

    @Bean
    public WebClient kakaoLocalWebClient() {
        return WebClient.builder()
                .defaultHeader("Authorization", "KakaoAK " + kakaoLocalProperties.secretKey())
                .baseUrl(kakaoLocalProperties.baseUrl())
                .build();
    }

    @Bean
    public WebClient kakaoMobilityWebClient() {
        return WebClient.builder()
                .defaultHeader("Authorization", "KakaoAK " + kakaoMobilityProperties.secretKey())
                .baseUrl(kakaoMobilityProperties.url())
                .build();
    }

    @Bean
    public WebClient googlePlaceWebClient() {
        return WebClient.builder()
                .defaultHeader("X-Goog-Api-Key", googlePlaceProperties.secretKey())
                .baseUrl(googlePlaceProperties.baseUrl())
                .build();
    }

    @Bean
    public WebClient clovaWebClient() {
        return WebClient.builder()
                .defaultHeader("Authorization", "Bearer " + clovaProperties.studioApiKey())
                .defaultHeader("X-NCP-CLOVASTUDIO-REQUEST-ID", clovaProperties.requestId())
                .baseUrl(clovaProperties.baseUrl())
                .build();
    }
}
