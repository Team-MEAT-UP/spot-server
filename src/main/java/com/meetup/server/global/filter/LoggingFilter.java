package com.meetup.server.global.filter;

import com.meetup.server.global.util.LoggingUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "TRACE_ID";
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/actuator",
            "/swagger-ui",
            "/swagger-resources",
            "/v3/api-docs",
            "/webjars",
            "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (shouldSkipLogging(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        String traceId = getOrGenerateTraceId(requestWrapper);
        MDC.put(TRACE_ID, traceId);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            LoggingUtil.logRequest(requestWrapper);
            LoggingUtil.logResponse(responseWrapper, duration);

            responseWrapper.copyBodyToResponse();
            MDC.clear();
        }
    }

    private boolean shouldSkipLogging(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return EXCLUDE_PATHS.stream().anyMatch(uri::startsWith);
    }

    private String getOrGenerateTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }
        return traceId;
    }
}
