package com.meetup.server.global.config;

import com.meetup.server.global.clients.clova.ClovaProperties;
import com.meetup.server.global.clients.google.place.GooglePlaceProperties;
import com.meetup.server.global.clients.kakao.local.KakaoLocalProperties;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final KakaoLocalProperties kakaoLocalProperties;
    private final KakaoMobilityProperties kakaoMobilityProperties;
    private final GooglePlaceProperties googlePlaceProperties;
    private final ClovaProperties clovaProperties;

    @Bean
    public RestClient kakaoLocalRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1500);
        requestFactory.setReadTimeout(1500);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "KakaoAK " + kakaoLocalProperties.secretKey())
                .baseUrl(kakaoLocalProperties.baseUrl())
                .build();
    }

    @Bean
    public RestClient odsayRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1500);
        requestFactory.setReadTimeout(2000);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public RestClient kakaoMobilityRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1500);
        requestFactory.setReadTimeout(2000);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "KakaoAK " + kakaoMobilityProperties.secretKey())
                .baseUrl(kakaoMobilityProperties.url())
                .build();
    }

    @Bean
    public RestClient googlePlaceRestClient() {
        return RestClient.builder()
                .defaultHeader("X-Goog-Api-Key", googlePlaceProperties.secretKey())
                .baseUrl(googlePlaceProperties.baseUrl())
                .build();
    }

    @Bean
    public RestClient clovaRestClient() {
        return RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + clovaProperties.studioApiKey())
                .defaultHeader("X-NCP-CLOVASTUDIO-REQUEST-ID", clovaProperties.requestId())
                .baseUrl(clovaProperties.baseUrl())
                .build();
    }
}
