package com.meetup.server.fixture;

import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.type.PlaceCategory;
import com.meetup.server.place.dto.response.PlaceDetailResponse;
import com.meetup.server.place.dto.response.PlaceResponse;
import com.meetup.server.place.dto.response.PlaceResponseList;
import com.meetup.server.review.domain.value.PlaceScore;
import com.meetup.server.startpoint.domain.type.Location;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class PlaceFixture {

    public static final UUID PLACE_ID = UUID.fromString("019c5b90-7e80-7875-857a-51bcf5db482a");
    public static final String PLACE_NAME = "스타벅스 선정릉역점";
    public static final PlaceCategory PLACE_CATEGORY = PlaceCategory.CAFE;
    public static final int DISTANCE = 250;
    public static final double AVERAGE_RATING = 4.3;
    public static final double GOOGLE_RATING = 4.5;
    public static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
    public static final LocalTime CLOSE_TIME = LocalTime.of(22, 0);

    public static Place getPlace() {
        return Place.builder()
                .kakaoPlaceId("1337065720")
                .googlePlaceId("ChIJNazwn-mkfDUR-RHbyQEqj2c")
                .category(PlaceCategory.CAFE)
                .name("놀숲 건대점")
                .googleRating(null)
                .images(List.of())
                .openingHours(List.of())
                .googleReviews(List.of())
                .location(Location.of(37.5406181573079, 127.06798560729))
                .point(CoordinateUtil.createPoint(127.06798560729, 37.5406181573079))
                .build();
    }

    public static PlaceResponse getPlaceResponse() {
        return PlaceResponse.builder()
                .id(PLACE_ID)
                .category(PLACE_CATEGORY)
                .name(PLACE_NAME)
                .image("https://example.com/image.jpg")
                .openTime(OPEN_TIME)
                .closeTime(CLOSE_TIME)
                .distance(DISTANCE)
                .averageRating(AVERAGE_RATING)
                .googleRating(GOOGLE_RATING)
                .placeScore(PlaceScore.of(4, 5, 3))
                .build();
    }

    public static PlaceResponseList getPlaceResponseList() {
        return new PlaceResponseList(
                "개발자 모임",
                "선릉역",
                getPlaceResponse(),
                List.of(getPlaceResponse())
        );
    }

    public static PlaceResponseList getPlaceResponseListForReview() {
        return new PlaceResponseList(
                "개발자 모임",
                "선릉역",
                getPlaceResponse(),
                null
        );
    }

    public static PlaceDetailResponse getPlaceDetailResponse() {
        return PlaceDetailResponse.builder()
                .id(PLACE_ID)
                .kakaoPlaceId("1337065720")
                .category(PLACE_CATEGORY)
                .name(PLACE_NAME)
                .images(List.of("https://example.com/image.jpg"))
                .openTime(OPEN_TIME)
                .closeTime(CLOSE_TIME)
                .distance(DISTANCE)
                .averageRating(AVERAGE_RATING)
                .placeScore(PlaceScore.of(4, 5, 3))
                .isConfirmed(true)
                .isChanged(false)
                .build();
    }
}
