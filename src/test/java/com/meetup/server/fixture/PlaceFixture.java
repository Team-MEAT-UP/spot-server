package com.meetup.server.fixture;

import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.type.PlaceCategory;
import com.meetup.server.startpoint.domain.type.Location;

import java.util.List;

public class PlaceFixture {

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
}
