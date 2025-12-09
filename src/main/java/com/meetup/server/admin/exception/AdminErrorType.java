package com.meetup.server.admin.exception;

import com.meetup.server.global.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminErrorType implements ErrorType {
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "관리자를 찾을 수 없습니다."),
    ADMIN_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 등록된 관리자입니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
