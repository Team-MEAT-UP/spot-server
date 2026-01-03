package com.meetup.server.user.infrastructure.jpa;

import com.meetup.server.user.domain.LogUserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LogUserLoginRepository extends JpaRepository<LogUserLogin, Long> {

    @Query("""
        SELECT COUNT(DISTINCT l.userId) FROM LogUserLogin l
        WHERE l.createdAt BETWEEN :startDateTime AND :endDateTime
        AND l.loginStatus = 'SUCCESS'
    """)
    long countUniqueLoginUsers(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
}
