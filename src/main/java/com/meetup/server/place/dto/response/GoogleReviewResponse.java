package com.meetup.server.place.dto.response;

import com.meetup.server.place.domain.value.GoogleReview;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Builder
public record GoogleReviewResponse(
        String nickname,
        String profileImage,
        LocalDate date,
        String day,
        String content
) {
    public static GoogleReviewResponse from(GoogleReview googleReview) {
        return GoogleReviewResponse.builder()
                .nickname(googleReview.author())
                .profileImage(googleReview.authorProfileImage())
                .date(Optional.ofNullable(googleReview.publishTime()).map(LocalDateTime::toLocalDate).orElse(null))
                .day(Optional.ofNullable(googleReview.publishTime()).map(LocalDateTime::getDayOfWeek).map(day -> day.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.KOREAN)).orElse(null))
                .content(googleReview.content())
                .build();
    }

    public static List<GoogleReviewResponse> fromList(List<GoogleReview> googleReviews) {
        return googleReviews.stream()
                .map(GoogleReviewResponse::from)
                .toList();
    }
}
