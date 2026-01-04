package com.meetup.server.user.implement;

import com.meetup.server.user.domain.LogUserLogin;
import com.meetup.server.user.domain.type.LoginStatus;
import com.meetup.server.user.infrastructure.jpa.LogUserLoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogUserLoginWriter {

    private final LogUserLoginRepository logUserLoginRepository;

    @Async
    public CompletableFuture<Void> save(Long userId, LoginStatus loginStatus, String ipAddress, String userAgent, String failReason) {
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
        return CompletableFuture.completedFuture(null);
    }
}
