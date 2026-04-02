package com.meetup.server.auth.presentation.filter;

import com.meetup.server.auth.application.AuthService;
import com.meetup.server.auth.dto.response.ReissueTokenResponse;
import com.meetup.server.auth.exception.AuthErrorType;
import com.meetup.server.auth.support.AuthenticationUtil;
import com.meetup.server.auth.support.CookieUtil;
import com.meetup.server.global.support.jwt.JwtTokenProvider;
import com.meetup.server.global.util.LoggingUtil;
import com.meetup.server.user.exception.UserException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationUtil authenticationUtil;
    private final CookieUtil cookieUtil;
    private final AuthService authService;

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

        String token = cookieUtil.getAccessTokenFromCookie(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            authenticationUtil.setAuthenticationFromRequest(request, token);
        } else {
            reAuthenticateWithRefreshToken(request, response);
        }

        filterChain.doFilter(request, response);
    }

    private void reAuthenticateWithRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            try {
                ReissueTokenResponse reissueTokenResponse = authService.reIssueToken(response, refreshToken);
                cookieUtil.setAccessTokenCookie(response, reissueTokenResponse.accessToken());
                authenticationUtil.setAuthenticationFromRequest(request, reissueTokenResponse.accessToken());
            } catch (UserException e) {
                LoggingUtil.logError("[ReissueFailed]", AuthErrorType.INVALID_REFRESH_TOKEN, e);
                cookieUtil.deleteAccessTokenCookie(response);
                cookieUtil.deleteRefreshTokenCookie(response);
            }
        }
    }

}
