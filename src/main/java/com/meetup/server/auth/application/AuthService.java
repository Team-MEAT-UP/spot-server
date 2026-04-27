package com.meetup.server.auth.application;

import com.meetup.server.auth.dto.response.ReissueTokenResponse;
import com.meetup.server.auth.exception.AuthErrorType;
import com.meetup.server.auth.exception.AuthException;
import com.meetup.server.auth.infrastructure.RefreshTokenRedisRepository;
import com.meetup.server.auth.support.CookieUtil;
import com.meetup.server.global.support.jwt.JwtTokenProvider;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.implement.UserReader;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;
    private final UserReader userReader;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        deleteRefreshTokenIfValid(request);
        cookieUtil.deleteAccessTokenCookie(response);
        cookieUtil.deleteRefreshTokenCookie(response);
    }

    public String createAccessTokenForUser(Long userId) {
        User user = userReader.read(userId);
        return jwtTokenProvider.createAccessToken(user);
    }

    public ReissueTokenResponse reIssueToken(String refreshToken) {
        refreshToken = resolveToken(refreshToken);

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorType.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.extractUserIdFromToken(refreshToken);
        User user = userReader.read(userId);

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        if (!refreshTokenRedisRepository.rotate(userId, refreshToken, newRefreshToken)) {
            throw new AuthException(AuthErrorType.INVALID_REFRESH_TOKEN);
        }

        return ReissueTokenResponse.from(accessToken, newRefreshToken);
    }

    private void deleteRefreshTokenIfValid(HttpServletRequest request) {
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return;
        }

        refreshToken = resolveToken(refreshToken);
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return;
        }

        Long userId = jwtTokenProvider.extractUserIdFromToken(refreshToken);
        refreshTokenRedisRepository.delete(userId);
    }

    private String resolveToken(String token) {
        if (token == null) {
            throw new AuthException(AuthErrorType.INVALID_REFRESH_TOKEN);
        }
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }
}
