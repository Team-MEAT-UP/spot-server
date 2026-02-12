package com.meetup.server.auth.presentation.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.server.auth.exception.AuthErrorType;
import com.meetup.server.global.support.response.ApiResponse;
import com.meetup.server.global.util.LoggingUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        LoggingUtil.logError("[Forbidden]", AuthErrorType.INVALID_PERMISSION, accessDeniedException);

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), ApiResponse.error(AuthErrorType.INVALID_TOKEN_ROLE));
    }
}
