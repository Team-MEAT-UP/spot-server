package com.meetup.server.place.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.value.GoogleReview;
import com.meetup.server.place.dto.response.*;
import com.meetup.server.place.implement.PlaceProcessor;
import com.meetup.server.place.implement.PlaceReader;
import com.meetup.server.place.implement.PlaceSorter;
import com.meetup.server.place.persistence.projection.PlaceWithDistance;
import com.meetup.server.review.domain.Review;
import com.meetup.server.review.implement.ReviewReader;
import com.meetup.server.review.persistence.projection.PlaceWithRating;
import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.implement.reader.SubwayReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceService {

    private static final double RADIUS = 500;

    private final PlaceReader placeReader;
    private final PlaceProcessor placeProcessor;
    private final EventReader eventReader;
    private final ReviewReader reviewReader;
    private final PlaceSorter placeSorter;
    private final SubwayReader subwayReader;

    @Transactional
    @CacheEvict(value = "routeDetails", key = "#eventId")
    public void confirmPlace(UUID eventId, UUID placeId, int subwayId) {
        Event event = eventReader.read(eventId);
        Place place = placeReader.read(placeId);
        Subway subway = subwayReader.read(subwayId);

        event.updateMeetingPlace(place, subway);
    }

    public PlaceResponseList getAllPlaces(UUID eventId, int subwayId) {
        Event event = eventReader.read(eventId);
        Subway subway = subwayReader.read(subwayId);

        Place confirmedPlace = event.getPlace();
        PlaceResponse confirmedPlaceResponse = null;
        if (confirmedPlace != null) {
            PlaceWithDistance confirmedPlaceWithDistance = placeReader.readWithDistance(confirmedPlace, subway.getPoint());
            PlaceWithRating confirmedPlaceWithRating = reviewReader.readPlaceRatingsAsMap(List.of(confirmedPlace.getId())).get(confirmedPlace);
            confirmedPlaceResponse = PlaceResponse.of(confirmedPlaceWithDistance, confirmedPlaceWithRating);
        }

        List<PlaceWithDistance> nearbyPlaces = placeReader.readAllWithinRadius(subway.getPoint(), RADIUS);
        List<PlaceResponse> recommendedPlaces = placeProcessor.getRecommendedPlaces(confirmedPlace, nearbyPlaces);

        return PlaceResponseList.of(event, subway, confirmedPlaceResponse, recommendedPlaces);
    }

    public PlaceDetailResponse getPlace(UUID eventId, UUID placeId, int subwayId) {
        Event event = eventReader.read(eventId);
        Place place = placeReader.read(placeId);
        Subway subway = subwayReader.read(subwayId);

        List<Review> reviews = placeSorter.sortSpotReviewsByNewest(reviewReader.readAll(place));
        List<GoogleReview> googleReviews = placeSorter.sortGoogleReviewByNewest(place.getGoogleReviews());

        PlaceWithDistance placeWithDistance = placeReader.readWithDistance(place, subway.getPoint());
        PlaceWithRating placeWithRating = reviewReader.readPlaceRatingsAsMap(List.of(place.getId())).get(place);
        PlaceResponse placeResponse = PlaceResponse.of(placeWithDistance, placeWithRating);

        boolean isConfirmed = event.getPlace() != null;
        boolean isChanged = event.getPlace() != null && !event.getPlace().isSamePlace(place);

        return PlaceDetailResponse.of(
                place,
                placeResponse,
                reviewReader.readQuietnessRating(place.getId()),
                ReviewResponse.fromList(reviews),
                GoogleReviewResponse.fromList(googleReviews),
                isConfirmed,
                isChanged
        );
    }

    public PlaceResponseList getPlaceForReview(UUID eventId) {
        Event event = eventReader.read(eventId);
        Subway subway = event.getSubway();
        Place place = event.getPlace();

        PlaceWithDistance placeWithDistance = placeReader.readWithDistance(place, subway.getPoint());
        PlaceWithRating placeWithRating = reviewReader.readPlaceRatingsAsMap(List.of(place.getId())).get(place);
        return PlaceResponseList.of(event, subway, PlaceResponse.of(placeWithDistance, placeWithRating), null);
    }
}
