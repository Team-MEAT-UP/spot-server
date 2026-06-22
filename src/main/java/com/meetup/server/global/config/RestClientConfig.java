package com.meetup.server.global.config;

import com.meetup.server.global.clients.clova.ClovaProperties;
import com.meetup.server.global.clients.google.place.GooglePlaceProperties;
import com.meetup.server.global.clients.kakao.local.KakaoLocalProperties;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityProperties;
import com.meetup.server.global.clients.toss.TossApiProperties;
import com.meetup.server.global.clients.toss.TossMtlsProperties;
import com.meetup.server.subway.infrastructure.api.SeoulSubwayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Base64;


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
    private final TossApiProperties tossApiProperties;
    private final TossMtlsProperties tossMtlsProperties;


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

    @Bean(name = "tossRestClient")
    @ConditionalOnProperty(
            prefix = "toss.mtls",
            name = "key-store-base64"
    )
    public RestClient tossRestClient() throws Exception {
        if (tossMtlsProperties.keyStoreBase64() == null || tossMtlsProperties.keyStoreBase64().isBlank()) {
            throw new IllegalStateException("TOSS_MTLS_KEY_STORE_BASE64 is missing");
        }

        if (tossMtlsProperties.keyStorePassword() == null || tossMtlsProperties.keyStorePassword().isBlank()) {
            throw new IllegalStateException("TOSS_MTLS_KEY_STORE_PASSWORD is missing");
        }

        String keyStoreBase64 = tossMtlsProperties.keyStoreBase64()
                .replaceAll("\\s", "");

        byte[] keyStoreBytes = Base64.getDecoder().decode(keyStoreBase64);

        KeyStore keyStore = KeyStore.getInstance(tossMtlsProperties.keyStoreType());

        char[] password = tossMtlsProperties.keyStorePassword().toCharArray();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(keyStoreBytes)) {
            keyStore.load(inputStream, password);
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
        );
        keyManagerFactory.init(keyStore, password);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                keyManagerFactory.getKeyManagers(),
                null,
                null
        );

        HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(20));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(tossApiProperties.baseUrl())
                .build();
    }

    @Bean(name = "tossRestClient")
    @ConditionalOnMissingBean(name = "tossRestClient")
    public RestClient fallbackTossRestClient() {
        return RestClient.builder()
                .baseUrl(tossApiProperties.baseUrl())
                .build();
    }
}
