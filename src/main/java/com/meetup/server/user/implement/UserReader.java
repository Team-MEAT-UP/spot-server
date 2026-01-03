package com.meetup.server.user.implement;

import com.meetup.server.user.domain.User;
import com.meetup.server.user.exception.UserErrorType;
import com.meetup.server.user.exception.UserException;
import com.meetup.server.user.infrastructure.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserReader {

    private final UserRepository userRepository;

    public Optional<User> readUserIfExists(Long userId) {
        if (userId == null) return Optional.empty();
        return userRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    public User read(Long userId) {
        return readUserIfExists(userId)
                .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
    }

    public Page<User> readAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public long readDailyRegisterUserCount(LocalDate todayDate) {
        LocalDateTime startDateTime = todayDate.atStartOfDay();
        LocalDateTime endDateTime = todayDate.atTime(LocalTime.MAX);
        return userRepository.countByCreatedAtBetween(startDateTime, endDateTime);
    }
}
