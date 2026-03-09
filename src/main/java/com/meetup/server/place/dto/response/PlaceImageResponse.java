package com.meetup.server.place.dto.response;

public record PlaceImageResponse(
        String imageUrl
) {
    public static PlaceImageResponse from(String imageUrl) {
        return new PlaceImageResponse(imageUrl);
    }
}
