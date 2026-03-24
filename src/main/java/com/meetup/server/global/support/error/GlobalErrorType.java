package com.meetup.server.global.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorType implements ErrorType {

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 내부 오류입니다."),
    NOT_FOUND_RESOURCE(HttpStatus.NOT_FOUND, "존재하지 않는 자원입니다."),
    FAILED_REQUEST_VALIDATION(HttpStatus.BAD_REQUEST, "요청 데이터 검증에 실패하였습니다."),
    INVALID_REQUEST_ARGUMENT(HttpStatus.BAD_REQUEST, "잘못된 요청 인자입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다."),
    IMAGE_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 변환 중 예외가 발생했습니다."),
    IMAGE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 예외가 발생했습니다."),
    REDIS_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "Redis 서버와의 연결에 실패했습니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
