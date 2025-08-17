package com.meetup.server.review.implement;

import com.meetup.server.place.domain.Place;
import com.meetup.server.review.domain.Review;
import com.meetup.server.review.infrastructure.jpa.ReviewRepository;
import com.meetup.server.review.infrastructure.jpa.VisitedReviewRepository;
import com.meetup.server.review.infrastructure.jpa.projection.PlaceQuietnessWithRating;
import com.meetup.server.review.infrastructure.jpa.projection.PlaceWithRating;
import com.meetup.server.startpoint.infrastructure.querydsl.projection.EventHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReviewReader {

    private final ReviewRepository reviewRepository;
    private final VisitedReviewRepository visitedReviewRepository;

    public List<Review> readAll(Place place) {
        return reviewRepository.findAllByPlace(place);
    }

    public List<PlaceWithRating> readPlaceRatings(List<UUID> placeIds) {
        return visitedReviewRepository.findPlaceRatingsByPlaceIds(placeIds);
    }

    public Map<Place, PlaceWithRating> readPlaceRatingsAsMap(List<UUID> placeIds) {
        return readPlaceRatings(placeIds)
                .stream()
                .collect(Collectors.toMap(PlaceWithRating::place, Function.identity()));
    }

    public PlaceQuietnessWithRating readQuietnessRating(UUID placeId) {
        return visitedReviewRepository.findQuietnessRatingByPlaceId(placeId);
    }

    public Map<UUID, Boolean> readReviewsWrittenByUser(List<EventHistory> projections, Long userId) {
        return reviewRepository.findReviewsWrittenByUser(projections, userId);
    }

    public List<Review> readAll() {
        return reviewRepository.findAll();
    }
}
