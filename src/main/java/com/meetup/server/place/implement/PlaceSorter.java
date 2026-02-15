package com.meetup.server.place.implement;

import com.meetup.server.place.domain.value.GoogleReview;
import com.meetup.server.place.dto.response.PlaceResponse;
import com.meetup.server.review.domain.Review;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class PlaceSorter {

    public List<PlaceResponse> sortByRating(List<PlaceResponse> placeResponses) {
        List<PlaceResponse> placeResponsesWithReview = placeResponses.stream()
                .filter(placeResponse -> placeResponse.averageRating() != null)
                .sorted(Comparator.comparing(PlaceResponse::averageRating).reversed())
                .toList();

        List<PlaceResponse> placeResponsesWithoutReview = placeResponses.stream()
                .filter(placeResponse -> placeResponse.averageRating() == null)
                .sorted(Comparator.comparing(
                        PlaceResponse::googleRating,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();

        List<PlaceResponse> mergedPlaceResponses = new ArrayList<>();
        mergedPlaceResponses.addAll(placeResponsesWithReview);
        mergedPlaceResponses.addAll(placeResponsesWithoutReview);
        return mergedPlaceResponses;
    }

    public List<Review> sortSpotReviewsByNewest(List<Review> reviews) {
        return reviews.stream()
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .toList();
    }

    public List<GoogleReview> sortGoogleReviewByNewest(List<GoogleReview> googleReviews) {
        return googleReviews.stream()
                .sorted(Comparator.comparing(
                        GoogleReview::publishTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }
}
