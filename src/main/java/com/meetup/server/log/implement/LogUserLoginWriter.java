package com.meetup.server.log.implement;

import com.meetup.server.log.domain.LogUserLogin;
import com.meetup.server.log.domain.type.LoginStatus;
import com.meetup.server.log.infrastructure.jpa.LogUserLoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogUserLoginWriter {

    private final LogUserLoginRepository logUserLoginRepository;

    @Async
    public void save(Long userId, LoginStatus loginStatus, String ipAddress, String userAgent, String failReason) {
        try {
            logUserLoginRepository.save(
                    LogUserLogin.builder()
                            .userId(userId)
                            .loginStatus(loginStatus)
                            .ipAddress(ipAddress)
                            .userAgent(userAgent)
                            .failReason(failReason)
                            .build()
            );
        } catch (Exception e) {
            log.error("[LogUserLoginWriter]: 유저 로그인 로그 저장에 실패했습니다. userId: {}", userId, e);
        }
    }
}
