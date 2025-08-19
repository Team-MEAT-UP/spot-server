package com.meetup.server.place.dto.response;

import com.meetup.server.review.infrastructure.jpa.projection.PlaceQuietnessWithRating;

public record PlaceQuietnessResponse(
        Integer morning,
        Integer lunch,
        Integer night
) {
    public static PlaceQuietnessResponse from(PlaceQuietnessWithRating rating) {
        if (rating == null) {
            return new PlaceQuietnessResponse(null, null, null);
        }

        return new PlaceQuietnessResponse(
                roundQuietnessOrElseNull(rating.morning()),
                roundQuietnessOrElseNull(rating.lunch()),
                roundQuietnessOrElseNull(rating.night())
        );
    }

    private static Integer roundQuietnessOrElseNull(Double value) {
        if (value == null) return null;
        return (int) Math.round(value);
    }
}
