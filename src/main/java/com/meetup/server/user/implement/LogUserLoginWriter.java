package com.meetup.server.user.implement;

import com.meetup.server.user.domain.LogUserLogin;
import com.meetup.server.user.domain.type.LoginStatus;
import com.meetup.server.user.infrastructure.jpa.LogUserLoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogUserLoginWriter {

    private final LogUserLoginRepository logUserLoginRepository;

    @Async
    public void save(Long userId, LoginStatus loginStatus, String ipAddress, String userAgent, String failReason) {
        logUserLoginRepository.save(
                LogUserLogin.builder()
                        .userId(userId)
                        .loginStatus(loginStatus)
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .failReason(failReason)
                        .build()
        );
    }
}
