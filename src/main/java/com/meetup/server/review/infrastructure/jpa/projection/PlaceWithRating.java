package com.meetup.server.review.infrastructure.jpa.projection;

import com.meetup.server.place.domain.Place;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Stream;

public record PlaceWithRating(
        Place place,
        Double avgSocket,
        Double avgSeat,
        Double avgQuiet
) {

    public Double calculateAverageRating() {
        OptionalDouble averageRating = Stream.of(this.avgSocket(), this.avgSeat(), this.avgQuiet())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();

        return averageRating.isPresent() ? Math.round(averageRating.getAsDouble() * 10.0) / 10.0 : null;
    }
}
