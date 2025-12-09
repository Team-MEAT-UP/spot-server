package com.meetup.server.admin.infrastructure.jpa;

import com.meetup.server.admin.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);
}
