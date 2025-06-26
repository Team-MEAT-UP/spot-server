package com.meetup.server.place.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import com.meetup.server.global.domain.BaseEntity;
import com.meetup.server.place.domain.type.PlaceCategory;
import com.meetup.server.place.domain.value.GoogleReview;
import com.meetup.server.place.domain.value.Image;
import com.meetup.server.place.domain.value.OpeningHour;
import com.meetup.server.startpoint.domain.type.Location;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "place", uniqueConstraints = {@UniqueConstraint(columnNames = {"kakao_place_id", "google_place_id"})})
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class Place extends BaseEntity {

    @Id
    @Column(name = "place_id")
    private UUID id;

    @Column(name = "kakao_place_id", nullable = false)
    private String kakaoPlaceId;

    @Column(name = "google_place_id", nullable = false)
    private String googlePlaceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PlaceCategory category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "google_rating")
    private Double googleRating;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Image> images;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<OpeningHour> openingHours;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<GoogleReview> googleReviews;

    @Embedded
    private Location location;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Point point;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private String rawJson;

    @PrePersist
    public void prePersist() {
        this.id = UuidCreator.getTimeOrderedEpoch();
    }

    @Builder
    public Place(String kakaoPlaceId, String googlePlaceId, PlaceCategory category, String name, Double googleRating, List<Image> images, List<OpeningHour> openingHours, List<GoogleReview> googleReviews, Location location, Point point, String rawJson) {
        this.kakaoPlaceId = kakaoPlaceId;
        this.googlePlaceId = googlePlaceId;
        this.category = category;
        this.name = name;
        this.googleRating = googleRating;
        this.images = images;
        this.openingHours = openingHours;
        this.googleReviews = googleReviews;
        this.location = location;
        this.point = point;
        this.rawJson = rawJson;
    }

    public boolean isSamePlace(Place place) {
        return this.id.equals(place.getId());
    }

    public void saveGoogleReviews(List<GoogleReview> googleReviews) {
        if (googleReviews == null || googleReviews.isEmpty()) {
            return;
        }
        this.googleReviews = googleReviews;
    }
}
