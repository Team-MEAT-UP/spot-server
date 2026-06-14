package com.meetup.server.auth.application;

import com.meetup.server.auth.dto.request.TossLoginRequest;
import com.meetup.server.auth.dto.response.TossGenerateTokenResponse;
import com.meetup.server.auth.dto.response.TossLoginMeResponse;
import com.meetup.server.auth.dto.response.TossLoginResponse;
import com.meetup.server.auth.support.CookieUtil;
import com.meetup.server.global.clients.toss.TossAuthClient;
import com.meetup.server.global.clients.toss.TossGenerateTokenRequest;
import com.meetup.server.global.support.jwt.JwtTokenProvider;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.domain.type.Role;
import com.meetup.server.user.infrastructure.jpa.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TossAuthService {
    private static final String TOSS_SOCIAL_ID_PREFIX = "toss:";

    private final TossAuthClient tossAuthClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    public TossLoginResponse login(
            TossLoginRequest request,
            HttpServletResponse response
    ) {
        if (request.authorizationCode() == null || request.authorizationCode().isBlank()) {
            throw new IllegalArgumentException("authorizationCode is required");
        }

        if (request.referrer() == null || request.referrer().isBlank()) {
            throw new IllegalArgumentException("referrer is required");
        }

        TossGenerateTokenResponse tokenResponse = tossAuthClient.generateToken(
                new TossGenerateTokenRequest(
                        request.authorizationCode(),
                        normalizeReferrer(request.referrer())
                )
        );

        if (tokenResponse == null) {
            throw new IllegalStateException("Toss generate-token response is null");
        }

        if (!"SUCCESS".equalsIgnoreCase(tokenResponse.resultType())) {
            throw new IllegalStateException(
                    "Toss generate-token failed: " +
                            (tokenResponse.error() != null
                                    ? tokenResponse.error().errorCode() + " / " + tokenResponse.error().reason()
                                    : "unknown error")
            );
        }

        if (tokenResponse.success() == null) {
            throw new IllegalStateException("Toss generate-token success body is null");
        }

        String tossAccessToken = tokenResponse.success().accessToken();

        if (tossAccessToken == null || tossAccessToken.isBlank()) {
            throw new IllegalStateException("Toss accessToken is null or blank");
        }

        TossLoginMeResponse meResponse = tossAuthClient.loginMe(tossAccessToken);

        if (meResponse == null) {
            throw new IllegalStateException("Toss login-me response is null");
        }

        if (!"SUCCESS".equalsIgnoreCase(meResponse.resultType())) {
            throw new IllegalStateException(
                    "Toss login-me failed: " +
                            (meResponse.error() != null
                                    ? meResponse.error().errorCode() + " / " + meResponse.error().reason()
                                    : "unknown error")
            );
        }

        if (meResponse.success() == null) {
            throw new IllegalStateException("Toss login-me success body is null");
        }

        Long userKey = meResponse.success().userKey();

        if (userKey == null) {
            throw new IllegalStateException("Toss userKey is null");
        }

        String socialId = TOSS_SOCIAL_ID_PREFIX + userKey;

        User user = getOrCreateUser(socialId);

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        cookieUtil.setAccessTokenCookie(response, accessToken);
        cookieUtil.setRefreshTokenCookie(response, refreshToken);

        return new TossLoginResponse(accessToken, refreshToken);
    }

    private String normalizeReferrer(String referrer) {
        if (referrer.equalsIgnoreCase("sandbox")) {
            return "sandbox";
        }

        if (referrer.equalsIgnoreCase("default")) {
            return "DEFAULT";
        }

        throw new IllegalArgumentException("invalid referrer: " + referrer);
    }

    private User getOrCreateUser(String socialId) {
        User userForJoin = User.builder()
                .socialId(socialId)
                .email(null)
                .nickname("토스사용자")
                .profileImage(null)
                .role(Role.USER)
                .build();

        return userRepository.findBySocialId(socialId)
                .map(user -> {
                    if (user.isDeleted()) {
                        user.rejoin(userForJoin);
                        return userRepository.save(user);
                    }
                    return user;
                })
                .orElseGet(() -> userRepository.save(userForJoin));
    }
}
