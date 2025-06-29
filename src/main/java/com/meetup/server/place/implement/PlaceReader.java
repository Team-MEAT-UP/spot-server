package com.meetup.server.place.implement;

import com.meetup.server.place.domain.Place;
import com.meetup.server.place.exception.PlaceErrorType;
import com.meetup.server.place.exception.PlaceException;
import com.meetup.server.place.persistence.PlaceRepository;
import com.meetup.server.place.persistence.projection.PlaceWithDistance;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaceReader {

    private final PlaceRepository placeRepository;

    public Place read(UUID placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new PlaceException(PlaceErrorType.PLACE_NOT_FOUND));
    }

    public PlaceWithDistance readWithDistance(Place place, Point point) {
        return placeRepository.findPlaceWithDistance(place.getId(), point);
    }

    public List<PlaceWithDistance> readAllWithinRadius(Point point, double radius) {
        return placeRepository.findAllWithinRadius(point, radius);
    }

    public List<Place> readAll() {
        return placeRepository.findAll();
    }
}
