package com.meetup.server.user.infrastructure.jpa.init;

import com.meetup.server.global.support.DummyDataInit;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.domain.type.Role;
import com.meetup.server.user.infrastructure.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
@Profile("local")
@Order(1)
@DummyDataInit
public class UserDummy implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("[USER]더미 데이터 존재");
        } else {
            ArrayList<User> userList = new ArrayList<>();

            User DUMMY_USER1 = User.builder()
                    .email("test1@naver.com")
                    .nickname("테스트1")
                    .role(Role.USER)
                    .socialId("test1")
                    .profileImage("https://example.com/test1.jpg")
                    .personalInfoAgreement(true)
                    .marketingAgreement(true)
                    .build();

            User DUMMY_USER2 = User.builder()
                    .email("test2@naver.com")
                    .nickname("테스트2")
                    .role(Role.USER)
                    .socialId("test2")
                    .profileImage("https://example.com/test2.jpg")
                    .personalInfoAgreement(true)
                    .marketingAgreement(true)
                    .build();

            User DUMMY_USER3 = User.builder()
                    .email("test3@naver.com")
                    .nickname("테스트3")
                    .role(Role.USER)
                    .socialId("test3")
                    .profileImage("https://example.com/test3.jpg")
                    .personalInfoAgreement(true)
                    .marketingAgreement(false)
                    .build();

            userList.add(DUMMY_USER1);
            userList.add(DUMMY_USER2);
            userList.add(DUMMY_USER3);

            userRepository.saveAll(userList);
        }
    }
}
