package com.meetup.server.admin.application;

import com.meetup.server.admin.domain.Admin;
import com.meetup.server.admin.implement.AdminReader;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDetailsService implements UserDetailsService {

    private final AdminReader adminReader;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminReader.readByUsername(username);

        switch (admin.getRole()) {
            case ADMIN_PENDING -> throw new DisabledException("승인 대기 중인 계정입니다.");
            case ADMIN_REJECTED -> throw new DisabledException("승인이 거부된 계정입니다.");
        }

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .roles(admin.getRole().name())
                .build();
    }
}
