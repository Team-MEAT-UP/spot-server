package com.meetup.server.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CompressFilter extends OncePerRequestFilter {

    private static final List<Pattern> ALLOWED_COMPRESS_ENDPOINT_PATTERNS = List.of(
            Pattern.compile("^/events/[^/]+$")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!shouldCompress(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        GzipHttpServletResponseWrapper gzipResponse = new GzipHttpServletResponseWrapper(response);
        try {
            filterChain.doFilter(request, gzipResponse);
        } finally {
            gzipResponse.finish();
        }
    }

    private boolean shouldCompress(HttpServletRequest request) {
        String acceptEncoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
        String uri = request.getRequestURI();
        String method = request.getMethod();

        boolean isGet = "GET".equalsIgnoreCase(method);

        return acceptEncoding != null
                && acceptEncoding.contains("gzip")
                && isGet
                && matchesAllowedUri(uri);
    }

    private boolean matchesAllowedUri(String uri) {
        return ALLOWED_COMPRESS_ENDPOINT_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matcher(uri).matches());
    }
}
