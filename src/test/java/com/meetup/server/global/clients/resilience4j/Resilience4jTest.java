package com.meetup.server.global.clients.resilience4j;

import com.meetup.server.global.clients.exception.ClientErrorType;
import com.meetup.server.global.clients.exception.ClientException;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityClient;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityRequest;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityResponse;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchClient;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchRequest;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchResponse;
import com.meetup.server.support.IntegrationTestContainer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Resilience4jTest extends IntegrationTestContainer {

    @MockitoBean
    private KakaoMobilityClient kakaoMobilityClient;

    @MockitoBean
    private OdsayTransitRouteSearchClient odsayTransitRouteSearchClient;

    private static final KakaoMobilityRequest kakaoMobilityRequest = KakaoMobilityRequest.builder()
            .origin("127.10764191124568,37.402464820205246")
            .destination("127.11056336672839,37.39419693653072")
            .build();

    private static final OdsayTransitRouteSearchRequest odsayTransitRouteSearchRequest = OdsayTransitRouteSearchRequest.builder()
            .sx("127.10764191124568")
            .sy("37.402464820205246")
            .ex("127.11056336672839")
            .ey("37.39419693653072")
            .build();

    private CircuitBreaker circuitBreaker;
    private Retry retry;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(1))
                .build();
        circuitBreaker = CircuitBreaker.of("testCircuitBreaker", circuitBreakerConfig);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .build();
        retry = Retry.of("testRetry", retryConfig);

        circuitBreaker.reset();
    }

    @Nested
    @DisplayName("Kakao Mobility Client")
    class KakaoMobilityClientTest {

        @Test
        @DisplayName("호출 성공 시 서킷 브레이커는 닫힌 상태를 유지한다")
        void shouldStayWhenCircuitIsClosedOnSuccessForKakaoMobility() {
            // given
            circuitBreaker.transitionToClosedState();
            when(kakaoMobilityClient.sendRequest(kakaoMobilityRequest))
                    .thenReturn(new KakaoMobilityResponse("transId", new ArrayList<>()));

            // when
            assertDoesNotThrow(() -> circuitBreaker.executeSupplier(() -> kakaoMobilityClient.sendRequest(kakaoMobilityRequest)));

            // then
            assertEquals(circuitBreaker.getState(), CircuitBreaker.State.CLOSED);
            verify(kakaoMobilityClient, times(1)).sendRequest(kakaoMobilityRequest);
        }

        @Test
        @DisplayName("타임아웃 발생 시, 재시도 로직이 실행된 후 실패한다")
        void shouldRetryOnTimeoutAndFailForKakaoMobility() {
            // given
            when(kakaoMobilityClient.sendRequest(kakaoMobilityRequest))
                    .thenThrow(new ResourceAccessException("Read timed out"));

            // when
            Supplier<KakaoMobilityResponse> supplierWithFallback = () -> {
                try {
                    return retry.executeSupplier(() -> kakaoMobilityClient.sendRequest(kakaoMobilityRequest));
                } catch (Throwable t) {
                    throw new ClientException(ClientErrorType.KAKAO_MOBILITY_SERVICE_UNAVAILABLE);
                }
            };

            // then
            ClientException exception = assertThrows(ClientException.class, supplierWithFallback::get);
            assertEquals(ClientErrorType.KAKAO_MOBILITY_SERVICE_UNAVAILABLE, exception.getErrorType());
            verify(kakaoMobilityClient, times(3)).sendRequest(kakaoMobilityRequest);
        }

        @Test
        @DisplayName("서킷 브레이커가 열린 상태일 때 호출은 즉시 실패한다")
        void shouldFailImmediatelyWhenCircuitIsOpenForKakaoMobility() {
            // given
            circuitBreaker.transitionToOpenState();
            when(kakaoMobilityClient.sendRequest(kakaoMobilityRequest))
                    .thenThrow(new ResourceAccessException("Read timed out"));

            // when
            Supplier<KakaoMobilityResponse> supplierWithFallback = () -> {
                try {
                    return circuitBreaker.executeSupplier(() -> kakaoMobilityClient.sendRequest(kakaoMobilityRequest));
                } catch (Throwable t) {
                    throw new ClientException(ClientErrorType.KAKAO_MOBILITY_SERVICE_UNAVAILABLE);
                }
            };

            // then
            ClientException exception = assertThrows(ClientException.class, supplierWithFallback::get);
            assertEquals(ClientErrorType.KAKAO_MOBILITY_SERVICE_UNAVAILABLE, exception.getErrorType());
            assertEquals(circuitBreaker.getState(), CircuitBreaker.State.OPEN);
            verify(kakaoMobilityClient, never()).sendRequest(kakaoMobilityRequest);
        }
    }

    @Nested
    @DisplayName("Odsay Client")
    class OdsayClientTest {

        @Test
        @DisplayName("호출 성공 시 서킷 브레이커는 닫힌 상태를 유지한다")
        void shouldStayWhenCircuitIsClosedOnSuccessForOdsay() {
            // given
            circuitBreaker.transitionToClosedState();
            when(odsayTransitRouteSearchClient.sendRequest(odsayTransitRouteSearchRequest))
                    .thenReturn(new OdsayTransitRouteSearchResponse(
                            new OdsayTransitRouteSearchResponse.TransitData(
                                    0, 0, 0, 0, 0, 0, 0, 0, new ArrayList<>()
                            )));

            // when
            assertDoesNotThrow(() -> circuitBreaker.executeSupplier(() -> odsayTransitRouteSearchClient.sendRequest(odsayTransitRouteSearchRequest)));

            // then
            assertEquals(circuitBreaker.getState(), CircuitBreaker.State.CLOSED);
            verify(odsayTransitRouteSearchClient, times(1)).sendRequest(odsayTransitRouteSearchRequest);
        }

        @Test
        @DisplayName("타임아웃 발생 시, 재시도 로직이 실행된 후 실패한다")
        void shouldRetryOnTimeoutAndFailForOdsay() {
            // given
            when(odsayTransitRouteSearchClient.sendRequest(odsayTransitRouteSearchRequest))
                    .thenThrow(new ResourceAccessException("Read timed out"));

            // when
            Supplier<OdsayTransitRouteSearchResponse> supplierWithFallback = () -> {
                try {
                    return retry.executeSupplier(() -> odsayTransitRouteSearchClient.sendRequest(odsayTransitRouteSearchRequest));
                } catch (Throwable t) {
                    throw new ClientException(ClientErrorType.ODSAY_SERVICE_UNAVAILABLE);
                }
            };

            // then
            ClientException exception = assertThrows(ClientException.class, supplierWithFallback::get);
            assertEquals(ClientErrorType.ODSAY_SERVICE_UNAVAILABLE, exception.getErrorType());
            verify(odsayTransitRouteSearchClient, times(3)).sendRequest(odsayTransitRouteSearchRequest);
        }

        @Test
        @DisplayName("서킷 브레이커가 열린 상태일 때 호출은 즉시 실패한다")
        void shouldFailImmediatelyWhenCircuitIsOpenForOdsay() {
            // given
            circuitBreaker.transitionToOpenState();
            when(odsayTransitRouteSearchClient.sendRequest(odsayTransitRouteSearchRequest))
                    .thenThrow(new ResourceAccessException("Read timed out"));

            // when
            Supplier<OdsayTransitRouteSearchResponse> supplierWithFallback = () -> {
                try {
                    return circuitBreaker.executeSupplier(() -> odsayTransitRouteSearchClient.sendRequest(odsayTransitRouteSearchRequest));
                } catch (Throwable t) {
                    throw new ClientException(ClientErrorType.ODSAY_SERVICE_UNAVAILABLE);
                }
            };

            // then
            ClientException exception = assertThrows(ClientException.class, supplierWithFallback::get);
            assertEquals(ClientErrorType.ODSAY_SERVICE_UNAVAILABLE, exception.getErrorType());
            assertEquals(circuitBreaker.getState(), CircuitBreaker.State.OPEN);
            verify(odsayTransitRouteSearchClient, never()).sendRequest(odsayTransitRouteSearchRequest);
        }
    }
}
