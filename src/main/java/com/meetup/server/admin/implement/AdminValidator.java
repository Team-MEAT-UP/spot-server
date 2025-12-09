package com.meetup.server.admin.implement;

import com.meetup.server.admin.exception.AdminErrorType;
import com.meetup.server.admin.exception.AdminException;
import com.meetup.server.admin.infrastructure.jpa.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminValidator {

    private final AdminRepository adminRepository;

    public void validateAdminNotAlreadyExists(String username) {
        if (adminRepository.existsByUsername(username)) {
            throw new AdminException(AdminErrorType.ADMIN_ALREADY_EXISTS);
        }
    }
}
