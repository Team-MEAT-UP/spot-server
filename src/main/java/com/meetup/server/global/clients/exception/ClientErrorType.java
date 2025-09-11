package com.meetup.server.global.clients.exception;

import com.meetup.server.global.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClientErrorType implements ErrorType {
    EXCEED_RATE_LIMIT_PER_DAY(HttpStatus.TOO_MANY_REQUESTS, "일일 제한 호출수를 초과하였습니다."),
    WARNING_RATE_LIMIT_PER_DAY(HttpStatus.TOO_MANY_REQUESTS, "일일 호출 한도에 임박했습니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
