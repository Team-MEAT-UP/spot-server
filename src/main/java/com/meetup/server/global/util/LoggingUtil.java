package com.meetup.server.global.util;

import com.meetup.server.global.support.error.ErrorType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggingUtil {

    private static final int HTTP_ERROR_THRESHOLD = 400;

    public static void logRequest(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String requestBody = getRequestBody(request);

        String msg = "[Request] Method=%s, Path=%s, QueryString=%s, Body=%s"
                .formatted(method, path, queryString, requestBody);
        log.info(msg);
    }

    public static void logResponse(ContentCachingResponseWrapper response, long duration) {
        String msg = "[Response] StatusCode=%d, Duration=%dms".formatted(response.getStatus(), duration);

        if (response.getStatus() >= HTTP_ERROR_THRESHOLD) {
            log.error(msg);
        } else {
            log.info(msg);
        }
    }

    public static void logError(String logTag, ErrorType errorType, Exception e) {
        String errorCode = ((Enum<?>) errorType).name();
        String message = errorType.getMessage();
        String exceptionType = e.getClass().getSimpleName();

        String errorBody = "{\"errorCode\": \"%s\", \"message\": \"%s\", \"type\": \"%s\"}"
                .formatted(errorCode, message, exceptionType);

        String logMsg = "%s ErrorCode=%d, Message=%s, Body=%s"
                .formatted(logTag, errorType.getStatus().value(), message, errorBody);

        log.error(logMsg, e);
    }

    public static String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) return "";
        try {
            String encoding = request.getCharacterEncoding();
            return new String(content, encoding);
        } catch (UnsupportedEncodingException e) {
            return new String(content, StandardCharsets.UTF_8);
        }
    }
}
