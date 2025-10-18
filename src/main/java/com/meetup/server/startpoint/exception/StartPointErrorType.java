package com.meetup.server.startpoint.exception;

import com.meetup.server.global.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StartPointErrorType implements ErrorType {
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."),
    INVALID_START_POINT(HttpStatus.BAD_REQUEST, "해당 이벤트에 속하지 않는 출발지입니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
