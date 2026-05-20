package com.meetup.server.auth.exception;

import com.meetup.server.global.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorType implements ErrorType {
    INVALID_KAKAO_ACCOUNT(HttpStatus.UNAUTHORIZED, "카카오 계정이 유효하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    INVALID_TOKEN_ROLE(HttpStatus.FORBIDDEN, "ROLE이 유효하지 않습니다."),
    INVALID_PERMISSION(HttpStatus.FORBIDDEN, "해당 요청을 수행할 권한이 없거나 유효하지 않습니다."),
    FAILED_OAUTH_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "소셜 로그인 인증 과정에서 오류가 발생했습니다."),
    FAILED_TOKEN_CREATION(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 생성 중 오류가 발생했습니다."),
    FAILED_REFRESH_TOKEN_DIGEST(HttpStatus.INTERNAL_SERVER_ERROR, "리프레시 토큰 처리 중 오류가 발생했습니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
