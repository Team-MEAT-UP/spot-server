package com.meetup.server.place.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.type.PlaceCategory;
import com.meetup.server.place.domain.value.Image;
import com.meetup.server.place.domain.value.OpeningHour;
import com.meetup.server.place.infrastructure.jpa.projection.PlaceWithDistance;
import com.meetup.server.review.domain.value.PlaceScore;
import com.meetup.server.review.infrastructure.jpa.projection.PlaceWithRating;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static com.meetup.server.global.util.TimeUtil.KST_ZONE_ID;

@Builder
public record PlaceResponse(
        @Schema(description = "장소 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "장소 카테고리", example = "CAFE")
        PlaceCategory category,

        @Schema(description = "장소 이름", example = "스타벅스 선정릉역점")
        String name,

        @Schema(description = "장소 대표 이미지 URL", example = "https://lh3.googleusercontent.com/places/ANXAkqGxgajYRWoJrqwPHTELnLzvWwd1YegG00cDfV9Bg2bro5gt8_z0i2HDzBlsj2u2p8WC9vpTEsnK-GKL1SD8Mxof9JwkzrYqaCk=s4800-w1000-h667")
        String image,

        @Schema(description = "영업 시작 시간", example = "08:00", type = "string", format = "HH:mm")
        @JsonFormat(pattern = "HH:mm")
        LocalTime openTime,

        @Schema(description = "영업 종료 시간", example = "22:00", type = "string", format = "HH:mm")
        @JsonFormat(pattern = "HH:mm")
        LocalTime closeTime,

        @Schema(description = "거리 (미터 단위)", example = "250")
        Integer distance,

        @Schema(description = "평균 평점", example = "4.3")
        Double averageRating,

        @Schema(description = "구글 평점", example = "4.5")
        Double googleRating,

        @Schema(description = "장소 점수")
        PlaceScore placeScore
) {
    public static PlaceResponse of(Place place, int distance, PlaceWithRating placeWithRating, Double googleRating) {
        Optional<OpeningHour> todayOpeningHour = place.getOpeningHours().stream()
                .filter(openingHour -> openingHour.isToday(LocalDateTime.now(KST_ZONE_ID)))
                .findFirst();

        return PlaceResponse.builder()
                .id(place.getId())
                .category(place.getCategory())
                .name(place.getName())
                .image(place.getImages().stream().findFirst().map(Image::photoUri).orElse(null))
                .openTime(todayOpeningHour.map(OpeningHour::openTime).orElse(null))
                .closeTime(todayOpeningHour.map(OpeningHour::closeTime).orElse(null))
                .distance(distance)
                .averageRating(
                        Optional.ofNullable(placeWithRating)
                                .map(PlaceWithRating::calculateAverageRating)
                                .orElse(null)
                )
                .googleRating(googleRating)
                .placeScore(
                        Optional.ofNullable(placeWithRating)
                                .map(scores -> PlaceScore.fromRoundedAverages(scores.avgSocket(), scores.avgSeat(), scores.avgQuiet()))
                                .orElse(null)
                )
                .build();
    }

    public static PlaceResponse of(PlaceWithDistance placeWithDistance, PlaceWithRating placeWithRating) {
        return of(placeWithDistance.place(), placeWithDistance.distance().intValue(), placeWithRating, placeWithDistance.place().getGoogleRating());
    }
}
