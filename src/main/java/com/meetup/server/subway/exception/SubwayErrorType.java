package com.meetup.server.subway.exception;

import com.meetup.server.global.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubwayErrorType implements ErrorType {
    SUBWAY_NOT_FOUND(HttpStatus.NOT_FOUND, "지하철역을 찾을 수 없습니다."),
    SEOUL_API_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서울 Open API 데이터 조회에 실패했습니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
