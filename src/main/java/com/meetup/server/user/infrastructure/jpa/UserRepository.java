package com.meetup.server.user.infrastructure.jpa;

import com.meetup.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialId(String socialId);

    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);
}
