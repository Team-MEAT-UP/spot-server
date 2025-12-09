package com.meetup.server.admin.implement;

import com.meetup.server.admin.domain.Admin;
import com.meetup.server.admin.exception.AdminErrorType;
import com.meetup.server.admin.exception.AdminException;
import com.meetup.server.admin.infrastructure.jpa.AdminRepository;
import com.meetup.server.user.domain.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminReader {

    private final AdminRepository adminRepository;

    public Admin readByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new AdminException(AdminErrorType.ADMIN_NOT_FOUND));
    }
}
