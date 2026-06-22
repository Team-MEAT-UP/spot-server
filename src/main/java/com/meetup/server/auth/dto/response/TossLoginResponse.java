package com.meetup.server.auth.dto.response;

import com.meetup.server.user.domain.type.AccountStatus;
import com.meetup.server.user.domain.type.LoginProvider;
import com.meetup.server.user.domain.type.NextAction;

public record TossLoginResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        LoginProvider provider,
        AccountStatus accountStatus,
        boolean emailRequired,
        boolean onboardingRequired,
        NextAction nextAction
) {
}
