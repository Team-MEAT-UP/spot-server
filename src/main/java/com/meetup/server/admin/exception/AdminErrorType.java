package com.meetup.server.admin.exception;

import com.meetup.server.global.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminErrorType implements ErrorType {
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "관리자를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
