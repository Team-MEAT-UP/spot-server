package com.meetup.server.startpoint.exception;

import com.meetup.server.global.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StartPointErrorType implements ErrorType {
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."),
    ODSAY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ODsay API 호출에 실패했습니다."),
    KAKAO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Kakao API 호출에 실패했습니다."),
    INVALID_START_POINT(HttpStatus.BAD_REQUEST, "해당 이벤트에 속하지 않는 출발지입니다.")
    ;

    private final HttpStatus status;

    private final String message;
}
