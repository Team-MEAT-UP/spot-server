package com.meetup.server.user.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER("ROLE_USER"),
    WITHDRAWN("ROLE_WITHDRAWN"),
    ADMIN("ROLE_ADMIN"),
    ADMIN_PENDING("ROLE_ADMIN_PENDING"),
    ADMIN_REJECTED("ROLE_ADMIN_REJECTED"),
    ;

    private final String authority;
}
