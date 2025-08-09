package com.meetup.server.user.application;


import com.meetup.server.fixture.UserFixture;
import com.meetup.server.support.IntegrationTestContainer;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.domain.type.Role;
import com.meetup.server.user.dto.response.UserProfileInfoResponse;
import com.meetup.server.user.implement.UserReader;
import com.meetup.server.user.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest extends IntegrationTestContainer {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @Autowired
    private UserService userService;

    @Autowired
    private UserReader userReader;

    @BeforeEach
    void setUp() {
        user = userRepository.save(UserFixture.getUser());
    }

    @Test
    void 사용자_정보를_조회한다() {
        Long userId = user.getUserId();

        UserProfileInfoResponse userProfileInfoResponse = userRepository.findById(userId)
                .map(UserProfileInfoResponse::from)
                .orElseThrow(() -> new RuntimeException("User not found"));

        assertThat(userProfileInfoResponse).isNotNull();
        assertThat(userProfileInfoResponse.userId()).isEqualTo(user.getUserId());
        assertThat(userProfileInfoResponse.nickname()).isEqualTo(user.getNickname());
    }

    @Test
    void 사용자_탈퇴한다() {
        Long userId = user.getUserId();

        userService.withdrawUser(userId);

        User updatedUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        assertThat(updatedUser.getRole()).isEqualTo(Role.WITHDRAWN);
        assertThat(updatedUser.getDeletedAt()).isNotNull();
        assertThat(updatedUser.getNickname()).isEqualTo("탈퇴한 사용자");
        assertThat(updatedUser.getEmail()).isEmpty();
    }

    @Test
    void 탈퇴한_사용자는_조회할_수_없다() {
        User user = UserFixture.getWithdrawnUser();
        userRepository.save(user);

        Long userId = user.getUserId();
        Optional<User> result = userReader.readUserIfExists(userId);

        assertThat(result).isEmpty();
    }
}

