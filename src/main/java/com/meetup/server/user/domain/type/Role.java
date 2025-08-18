package com.meetup.server.user.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    WITHDRAWN("ROLE_WITHDRAWN"),
    ;

    private final String authority;
}
