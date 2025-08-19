package com.meetup.server.place.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.type.PlaceCategory;
import com.meetup.server.place.domain.value.Image;
import com.meetup.server.review.domain.value.PlaceScore;
import com.meetup.server.review.infrastructure.jpa.projection.PlaceQuietnessWithRating;
import lombok.Builder;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Builder
public record PlaceDetailResponse(
        UUID id,
        String kakaoPlaceId,
        PlaceCategory category,
        String name,
        List<String> images,
        @JsonFormat(pattern = "HH:mm")
        LocalTime openTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime closeTime,
        int distance,
        Double averageRating,
        PlaceQuietnessResponse placeQuietnessResponse,
        PlaceScore placeScore,
        List<ReviewResponse> reviews,
        List<GoogleReviewResponse> googleReviews,
        boolean isConfirmed,
        boolean isChanged
) {
    public static PlaceDetailResponse of(Place place,
                                         PlaceResponse placeResponse,
                                         PlaceQuietnessWithRating placeQuietnessWithRating,
                                         List<ReviewResponse> reviewResponses,
                                         List<GoogleReviewResponse> googleReviewResponses,
                                         boolean isConfirmed,
                                         boolean isChanged) {

        return PlaceDetailResponse.builder()
                .id(placeResponse.id())
                .kakaoPlaceId(place.getKakaoPlaceId())
                .category(placeResponse.category())
                .name(placeResponse.name())
                .images(place.getImages().stream()
                        .map(Image::photoUri)
                        .toList())
                .openTime(placeResponse.openTime())
                .closeTime(placeResponse.closeTime())
                .distance(placeResponse.distance())
                .averageRating(placeResponse.averageRating())
                .placeScore(placeResponse.placeScore())
                .placeQuietnessResponse(PlaceQuietnessResponse.from(placeQuietnessWithRating))
                .reviews(reviewResponses)
                .googleReviews(googleReviewResponses)
                .isConfirmed(isConfirmed)
                .isChanged(isChanged)
                .build();
    }
}
