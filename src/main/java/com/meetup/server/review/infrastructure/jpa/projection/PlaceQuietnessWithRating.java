package com.meetup.server.review.infrastructure.jpa.projection;

public record PlaceQuietnessWithRating(
        Double morning,
        Double lunch,
        Double night
) {
}
