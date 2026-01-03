package com.meetup.server.admin.dto.response;

import com.meetup.server.user.domain.User;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long userId,
        String nickname,
        String email,
        boolean isPersonalInfoAgreement,
        boolean isMarketingAgreement,
        LocalDateTime createdDateTime
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getUserId(),
                user.getNickname(),
                user.getEmail(),
                user.isPersonalInfoAgreement(),
                user.isMarketingAgreement(),
                user.getCreatedAt()
        );
    }
}
