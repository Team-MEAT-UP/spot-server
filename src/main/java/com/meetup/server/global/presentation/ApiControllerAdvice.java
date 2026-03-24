package com.meetup.server.global.presentation;

import com.meetup.server.global.support.error.GlobalErrorType;
import com.meetup.server.global.support.error.GlobalException;
import com.meetup.server.global.support.error.discord.DiscordAlarmSender;
import com.meetup.server.global.support.response.ApiResponse;
import com.meetup.server.global.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.redisson.client.RedisConnectionException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiControllerAdvice {

    private final DiscordAlarmSender discordAlarmSender;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        LoggingUtil.logError("[Exception]", GlobalErrorType.INTERNAL_ERROR, e);
        discordAlarmSender.sendErrorAlert(e);
        return new ResponseEntity<>(ApiResponse.error(GlobalErrorType.INTERNAL_ERROR), GlobalErrorType.INTERNAL_ERROR.getStatus());
    }

    @ExceptionHandler({RedisConnectionFailureException.class, RedisConnectionException.class})
    public ResponseEntity<ApiResponse<?>> handleRedisConnectionException(Exception e) {
        LoggingUtil.logError("[RedisConnectionException]", GlobalErrorType.REDIS_CONNECTION_ERROR, e);
        discordAlarmSender.sendErrorAlert(e);
        return new ResponseEntity<>(ApiResponse.error(GlobalErrorType.REDIS_CONNECTION_ERROR), GlobalErrorType.REDIS_CONNECTION_ERROR.getStatus());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResourceFoundException(NoResourceFoundException e) {
        return new ResponseEntity<>(ApiResponse.error(GlobalErrorType.NOT_FOUND_RESOURCE), GlobalErrorType.NOT_FOUND_RESOURCE.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        LoggingUtil.logError("[MethodArgumentNotValidException]", GlobalErrorType.FAILED_REQUEST_VALIDATION, e);
        return new ResponseEntity<>(ApiResponse.error(GlobalErrorType.FAILED_REQUEST_VALIDATION), GlobalErrorType.FAILED_REQUEST_VALIDATION.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        LoggingUtil.logError("[IllegalArgumentException]", GlobalErrorType.INVALID_REQUEST_ARGUMENT, e);
        return new ResponseEntity<>(ApiResponse.error(GlobalErrorType.INVALID_REQUEST_ARGUMENT), GlobalErrorType.INVALID_REQUEST_ARGUMENT.getStatus());
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(GlobalException e) {
        LoggingUtil.logError(String.format("[%s]", e.getClass().getSimpleName()), e.getErrorType(), e);
        return new ResponseEntity<>(ApiResponse.error(e.getErrorType()), e.getErrorType().getStatus());
    }

}
