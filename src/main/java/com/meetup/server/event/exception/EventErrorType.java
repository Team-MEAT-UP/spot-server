package com.meetup.server.event.exception;

import com.meetup.server.global.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EventErrorType implements ErrorType {
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."),
    START_POINT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "출발지는 최대 8개까지 등록할 수 있습니다."),
    INSUFFICIENT_START_POINTS(HttpStatus.BAD_REQUEST, "출발지는 최소 2개 이상이어야 합니다."),
    SAME_SUBWAY_FOR_ALL_START_POINTS(HttpStatus.BAD_REQUEST, "모든 출발지의 지하철역이 동일합니다."),
    NO_INTERMEDIATE_SUBWAYS_FOUND(HttpStatus.NOT_FOUND, "중간 지점 근처에 지하철역을 찾을 수 없습니다."),
    PATH_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "중간 지점 경로 계산에 실패했습니다."),
    ROUTE_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "경로 조회에 실패했습니다."),
    CACHE_NOT_FOUND(HttpStatus.NOT_FOUND, "캐시가 존재하지 않습니다.")
    ;

    private final HttpStatus status;

    private final String message;
}
