package com.meetup.server.place.infrastructure.jpa.projection;

import com.meetup.server.place.domain.Place;

public record PlaceWithDistance(
        Place place,
        Double distance
) {
}
