package com.meetup.server.global.config;

import com.meetup.server.global.clients.clova.ClovaProperties;
import com.meetup.server.global.clients.google.place.GooglePlaceProperties;
import com.meetup.server.global.clients.kakao.local.KakaoLocalProperties;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityProperties;
import com.meetup.server.subway.infrastructure.api.SeoulSubwayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private static final int KAKAO_LOCAL_CONNECT_TIMEOUT = 1500;
    private static final int KAKAO_LOCAL_READ_TIMEOUT = 1500;
    private static final int ODSAY_CONNECT_TIMEOUT = 1500;
    private static final int ODSAY_READ_TIMEOUT = 2000;
    private static final int KAKAO_MOBILITY_CONNECT_TIMEOUT = 1500;
    private static final int KAKAO_MOBILITY_READ_TIMEOUT = 2000;
    private static final int SEOUL_SUBWAY_CONNECT_TIMEOUT = 5000;
    private static final int SEOUL_SUBWAY_READ_TIMEOUT = 5000;

    private final KakaoLocalProperties kakaoLocalProperties;
    private final KakaoMobilityProperties kakaoMobilityProperties;
    private final GooglePlaceProperties googlePlaceProperties;
    private final ClovaProperties clovaProperties;
    private final SeoulSubwayProperties seoulSubwayProperties;

    @Bean
    public RestClient kakaoLocalRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(KAKAO_LOCAL_CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(KAKAO_LOCAL_READ_TIMEOUT);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "KakaoAK " + kakaoLocalProperties.secretKey())
                .baseUrl(kakaoLocalProperties.baseUrl())
                .build();
    }

    @Bean
    public RestClient odsayRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(ODSAY_CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(ODSAY_READ_TIMEOUT);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public RestClient kakaoMobilityRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(KAKAO_MOBILITY_CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(KAKAO_MOBILITY_READ_TIMEOUT);

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

    @Bean
    public RestClient seoulSubwayRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(SEOUL_SUBWAY_CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(SEOUL_SUBWAY_READ_TIMEOUT);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(seoulSubwayProperties.url())
                .build();
    }
}
