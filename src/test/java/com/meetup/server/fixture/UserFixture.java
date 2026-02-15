package com.meetup.server.fixture;

import com.meetup.server.user.domain.User;
import com.meetup.server.user.domain.type.Role;
import com.meetup.server.user.dto.request.UserAgreementRequest;
import com.meetup.server.user.dto.response.UserEventHistoryResponseList;
import com.meetup.server.user.dto.response.UserProfileInfoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UserFixture {

    public static final Long USER_ID = 1L;
    public static final String TEST_NICKNAME = "땡수팟";
    public static final String NEW_NICKNAME = "새닉네임";

    public static User getUser() {
        String uuid = UUID.randomUUID().toString();
        return User.builder()
                .userId(USER_ID)
                .nickname("땡수팟")
                .socialId("kakao" + uuid)
                .email(uuid + "@spot.com")
                .role(Role.USER)
                .build();
    }

    public static User getNewUser() {
        String uuid = UUID.randomUUID().toString();
        return User.builder()
                .nickname("김아무개")
                .socialId("kakao" + uuid)
                .email(uuid + "@spot.com")
                .role(Role.USER)
                .personalInfoAgreement(true)
                .marketingAgreement(true)
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
                .build();
    }

    public static UserProfileInfoResponse getUserProfileInfoResponse() {
        return UserProfileInfoResponse.from(getUser());
    }

    public static UserAgreementRequest getUserAgreementRequest() {
        return new UserAgreementRequest(true, true);
    }

    public static UserEventHistoryResponseList getUserEventHistoryResponseList() {
        return UserEventHistoryResponseList.of(List.of(), false, null);
    }
}
