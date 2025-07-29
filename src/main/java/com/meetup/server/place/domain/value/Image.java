package com.meetup.server.place.domain.value;

public record Image(
        String photoUri
) {
    public static Image from(String photoUri) {
        return new Image(photoUri);
    }
}
