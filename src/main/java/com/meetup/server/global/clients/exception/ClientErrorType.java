package com.meetup.server.global.clients.exception;

import com.meetup.server.global.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClientErrorType implements ErrorType {
    ODSAY_EXCEED_RATE_LIMIT_PER_DAY(HttpStatus.TOO_MANY_REQUESTS, "Odsay API 일일 제한 호출수를 초과하였습니다."),
    KAKAO_MOBILITY_EXCEED_RATE_LIMIT_PER_DAY(HttpStatus.TOO_MANY_REQUESTS, "카카오 모빌리티 API 일일 제한 호출수를 초과하였습니다."),

    ODSAY_WARNING_RATE_LIMIT_PER_DAY(HttpStatus.TOO_MANY_REQUESTS, "Odsay API 일일 호출 한도에 임박했습니다."),
    KAKAO_MOBILITY_WARNING_RATE_LIMIT_PER_DAY(HttpStatus.TOO_MANY_REQUESTS, "카카오 모빌리티 API 일일 호출 한도에 임박했습니다."),

    ODSAY_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Odsay API 서버가 일시적으로 사용 불가능합니다."),
    KAKAO_MOBILITY_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "카카오 모빌리티 API 서버가 일시적으로 사용 불가능합니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
