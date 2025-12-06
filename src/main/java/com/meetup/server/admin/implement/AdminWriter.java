package com.meetup.server.admin.implement;

import com.meetup.server.admin.domain.Admin;
import com.meetup.server.admin.infrastructure.jpa.AdminRepository;
import com.meetup.server.user.domain.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminWriter {

    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;

    public void save(String name, String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);

        Admin admin = Admin.builder()
                .name(name)
                .username(username)
                .password(encodedPassword)
                .role(Role.ADMIN_PENDING)
                .build();

        adminRepository.save(admin);
    }
}
