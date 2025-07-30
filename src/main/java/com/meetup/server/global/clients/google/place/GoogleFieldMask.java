package com.meetup.server.global.clients.google.place;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GoogleFieldMask {

    ALL("*"),
    PHOTOS("places.photos"),
    ;

    private final String mask;
}
