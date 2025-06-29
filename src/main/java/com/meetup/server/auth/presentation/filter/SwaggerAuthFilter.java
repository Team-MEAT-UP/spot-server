package com.meetup.server.auth.presentation.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
@Profile("!local")
public class SwaggerAuthFilter extends OncePerRequestFilter {

    @Value("${swagger.id}")
    private String ADMIN_ID;

    @Value("${swagger.pw}")
    private String ADMIN_PW;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final String[] SWAGGER_LIST = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (isSwaggerRequest(request.getRequestURI())) {
            String headerWithIDPW = request.getHeader("Authorization");

            if (!isValidBasicAuth(headerWithIDPW)) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Swagger API\"");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isSwaggerRequest(String uri) {
        for (String pattern : SWAGGER_LIST) {
            if (pathMatcher.match(pattern, uri)) return true;
        }
        return false;
    }

    private boolean isValidBasicAuth(String header) {
        if (header == null || !header.startsWith("Basic ")) {
            return false;
        }

        String base64Credentials = header.substring("Basic ".length());
        String[] credentials = decodeCredentials(base64Credentials);
        if (credentials == null || credentials.length != 2) {
            return false;
        }

        return ADMIN_ID.equals(credentials[0]) && ADMIN_PW.equals(credentials[1]);
    }

    private String[] decodeCredentials(String base64) {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64));
            return decoded.split(":", 2);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
