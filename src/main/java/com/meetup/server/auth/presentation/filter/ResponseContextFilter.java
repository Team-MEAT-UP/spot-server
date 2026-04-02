package com.meetup.server.auth.presentation.filter;

import com.meetup.server.auth.application.AuthService;
import com.meetup.server.auth.dto.response.ReissueTokenResponse;
import com.meetup.server.auth.support.CookieUtil;
import com.meetup.server.user.exception.UserException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseContextFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/admins");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            try {
                ReissueTokenResponse reissueTokenResponse = authService.reIssueToken(response, refreshToken);
                setResponseCookies(response, reissueTokenResponse);

            } catch (UserException e) {
                log.error("token is invalidate and check user: {}", e.getMessage());
                cookieUtil.deleteAccessTokenCookie(response);
                cookieUtil.deleteRefreshTokenCookie(response);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setResponseCookies(HttpServletResponse response, ReissueTokenResponse reissueTokenResponse) {
        cookieUtil.setRefreshTokenCookie(response, reissueTokenResponse.refreshToken());
    }
}
