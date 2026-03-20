package com.meetup.server.place.dto.response;

public record PlaceImageResponse(
        String image
) {
    public static PlaceImageResponse from(String image) {
        return new PlaceImageResponse(image);
    }
}
