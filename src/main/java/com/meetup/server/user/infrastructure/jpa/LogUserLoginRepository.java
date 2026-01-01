package com.meetup.server.user.infrastructure.jpa;

import com.meetup.server.user.domain.LogUserLogin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogUserLoginRepository extends JpaRepository<LogUserLogin, Long> {
}
