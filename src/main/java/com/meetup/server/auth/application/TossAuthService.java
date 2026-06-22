package com.meetup.server.auth.application;

import com.meetup.server.auth.dto.request.TossLoginRequest;
import com.meetup.server.auth.dto.response.TossGenerateTokenResponse;
import com.meetup.server.auth.dto.response.TossLoginMeResponse;
import com.meetup.server.auth.dto.response.TossLoginResponse;
import com.meetup.server.auth.support.CookieUtil;
import com.meetup.server.global.clients.toss.TossAuthClient;
import com.meetup.server.global.clients.toss.TossGenerateTokenRequest;
import com.meetup.server.global.clients.toss.TossPersonalInfoDecryptor;
import com.meetup.server.global.support.jwt.JwtTokenProvider;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.domain.type.AccountStatus;
import com.meetup.server.user.domain.type.LoginProvider;
import com.meetup.server.user.domain.type.NextAction;
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
    private static final String TOSS_DEFAULT_NICKNAME = "토스사용자";

    private final TossAuthClient tossAuthClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;
    private final TossPersonalInfoDecryptor tossPersonalInfoDecryptor;

    public TossLoginResponse login(
            TossLoginRequest request,
            HttpServletResponse response
    ) {
        validateRequest(request);

        TossGenerateTokenResponse tokenResponse = tossAuthClient.generateToken(
                new TossGenerateTokenRequest(
                        request.authorizationCode(),
                        normalizeReferrer(request.referrer())
                )
        );

        validateGenerateTokenResponse(tokenResponse);

        String tossAccessToken = tokenResponse.success().accessToken();

        if (tossAccessToken == null || tossAccessToken.isBlank()) {
            throw new IllegalStateException("Toss accessToken is null or blank");
        }

        TossLoginMeResponse meResponse = tossAuthClient.loginMe(tossAccessToken);

        validateLoginMeResponse(meResponse);

        Long userKey = meResponse.success().userKey();

        if (userKey == null) {
            throw new IllegalStateException("Toss userKey is null");
        }

        LoginUserResult loginResult = getOrCreateUser(userKey, meResponse);
        User user = loginResult.user();

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        cookieUtil.setAccessTokenCookie(response, accessToken);
        cookieUtil.setRefreshTokenCookie(response, refreshToken);

        boolean emailRequired = user.getEmail() == null || user.getEmail().isBlank();
        boolean onboardingRequired = isOnboardingRequired(loginResult.accountStatus());
        NextAction nextAction = resolveNextAction(loginResult.accountStatus());

        return new TossLoginResponse(
                accessToken,
                refreshToken,
                user.getUserId(),
                LoginProvider.TOSS,
                loginResult.accountStatus(),
                emailRequired,
                onboardingRequired,
                nextAction
        );
    }

    private void validateRequest(TossLoginRequest request) {
        if (request.authorizationCode() == null || request.authorizationCode().isBlank()) {
            throw new IllegalArgumentException("authorizationCode is required");
        }

        if (request.referrer() == null || request.referrer().isBlank()) {
            throw new IllegalArgumentException("referrer is required");
        }
    }

    private void validateGenerateTokenResponse(TossGenerateTokenResponse tokenResponse) {
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
    }

    private void validateLoginMeResponse(TossLoginMeResponse meResponse) {
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
    }

    private String normalizeReferrer(String referrer) {
        if (referrer.equalsIgnoreCase("sandbox")) {
            return "SANDBOX";
        }

        if (referrer.equalsIgnoreCase("default")) {
            return "DEFAULT";
        }

        throw new IllegalArgumentException("invalid referrer: " + referrer);
    }

    private LoginUserResult getOrCreateUser(
            Long userKey,
            TossLoginMeResponse meResponse
    ) {
        String socialId = createTossSocialId(userKey);
        User userForJoin = createTossUser(socialId, meResponse);

        return userRepository.findBySocialId(socialId)
                .map(user -> {
                    if (user.isDeleted()) {
                        user.rejoin(userForJoin);
                        User savedUser = userRepository.save(user);

                        return new LoginUserResult(savedUser, AccountStatus.REJOINED_USER);
                    }

                    return new LoginUserResult(user, AccountStatus.EXISTING_USER);
                })
                .orElseGet(() -> {
                    User savedUser = userRepository.save(userForJoin);

                    return new LoginUserResult(savedUser, AccountStatus.NEW_USER);
                });
    }

    private User createTossUser(
            String socialId,
            TossLoginMeResponse meResponse
    ) {
        return User.builder()
                .socialId(socialId)
                .email(null)
                .nickname(resolveTossNickname(meResponse))
                .profileImage(null)
                .role(Role.USER)
                .personalInfoAgreement(false)
                .marketingAgreement(false)
                .build();
    }

    private String resolveTossNickname(TossLoginMeResponse meResponse) {
        String encryptedName = meResponse.success().name();

        String decryptedName = tossPersonalInfoDecryptor.decryptOrNull(encryptedName);

        if (decryptedName == null || decryptedName.isBlank()) {
            return TOSS_DEFAULT_NICKNAME;
        }

        return decryptedName;
    }

    private String createTossSocialId(Long userKey) {
        return TOSS_SOCIAL_ID_PREFIX + userKey;
    }

    private boolean isOnboardingRequired(AccountStatus accountStatus) {
        return accountStatus == AccountStatus.NEW_USER
                || accountStatus == AccountStatus.REJOINED_USER;
    }

    private NextAction resolveNextAction(AccountStatus accountStatus) {
        if (accountStatus == AccountStatus.NEW_USER
                || accountStatus == AccountStatus.REJOINED_USER) {
            return NextAction.COMPLETE_PROFILE;
        }

        return NextAction.GO_HOME;
    }

    private record LoginUserResult(
            User user,
            AccountStatus accountStatus
    ) {
    }
}
