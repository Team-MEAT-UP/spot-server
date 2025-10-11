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
    START_POINT_CONFLICT(HttpStatus.CONFLICT, "출발지 생성이 동시에 요청되었습니다. 잠시 후 다시 시도해주세요."),
    ;

    private final HttpStatus status;

    private final String message;
}
