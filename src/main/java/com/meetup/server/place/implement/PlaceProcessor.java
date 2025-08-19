package com.meetup.server.place.implement;

import com.meetup.server.place.domain.Place;
import com.meetup.server.place.dto.response.PlaceResponse;
import com.meetup.server.place.infrastructure.jpa.projection.PlaceWithDistance;
import com.meetup.server.review.implement.ReviewReader;
import com.meetup.server.review.infrastructure.jpa.projection.PlaceWithRating;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlaceProcessor {

    private final PlaceSorter placeSorter;
    private final ReviewReader reviewReader;

    public List<PlaceResponse> getRecommendedPlaces(Place confirmedPlace, List<PlaceWithDistance> places) {
        List<PlaceWithDistance> filtered = places.stream()
                .filter(placeWithDistance -> confirmedPlace == null || !placeWithDistance.place().isSamePlace(confirmedPlace))
                .toList();

        List<UUID> placeIds = collectPlaceIds(confirmedPlace, places);
        Map<Place, PlaceWithRating> placeRatingsMap = reviewReader.readPlaceRatingsAsMap(placeIds);

        List<PlaceResponse> placeResponses = filtered.stream()
                .map(placeWithDistance -> {
                    PlaceWithRating placeWithRating = placeRatingsMap.get(placeWithDistance.place());
                    return PlaceResponse.of(placeWithDistance, placeWithRating);
                })
                .toList();

        return placeSorter.sortByRating(placeResponses);
    }

    private List<UUID> collectPlaceIds(Place confirmedPlace, List<PlaceWithDistance> places) {
        List<UUID> placeIds = places.stream()
                .map(placeWithDistance -> placeWithDistance.place().getId())
                .collect(Collectors.toList());

        if (confirmedPlace != null && !placeIds.contains(confirmedPlace.getId())) {
            placeIds.add(confirmedPlace.getId());
        }
        return placeIds;
    }

}
