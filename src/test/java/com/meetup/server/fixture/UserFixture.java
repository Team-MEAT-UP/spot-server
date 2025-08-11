package com.meetup.server.fixture;

import com.meetup.server.user.domain.User;
import com.meetup.server.user.domain.type.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserFixture {

    public static User getUser() {
        String uuid = UUID.randomUUID().toString();
        return User.builder()
                .nickname("땡수팟")
                .socialId("kakao" + uuid)
                .email(uuid + "@spot.com")
                .role(Role.USER)
                .isDeleted(false)
                .build();
    }

    public static User getWithdrawnUser() {
        String uuid = UUID.randomUUID().toString();
        return User.builder()
                .nickname("탈퇴한 사용자")
                .socialId("kakao" + uuid)
                .email(uuid + "@spot.com")
                .role(Role.WITHDRAWN)
                .personalInfoAgreement(false)
                .marketingAgreement(false)
                .deletedAt(LocalDateTime.now())
                .isDeleted(true)
                .build();
    }
}
