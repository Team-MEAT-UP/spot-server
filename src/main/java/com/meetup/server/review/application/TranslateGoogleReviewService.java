package com.meetup.server.review.application;

import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.value.GoogleReview;
import com.meetup.server.place.implement.PlaceReader;
import com.meetup.server.place.implement.PlaceWriter;
import com.meetup.server.review.implement.ReviewTranslator;
import com.meetup.server.review.util.TextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateGoogleReviewService {

    private final PlaceReader placeReader;
    private final ReviewTranslator reviewTranslator;
    private final PlaceWriter placeWriter;


    @Transactional
    public void saveReviewForKor() {
        List<Place> places = placeReader.readAll();

        for (Place place : places) {
            List<GoogleReview> originalReviews = place.getGoogleReviews();
            if (CollectionUtils.isEmpty(originalReviews)) {
                continue;
            }

            List<GoogleReview> translatedReviews = translateGoogleReviews(originalReviews);

            if (hasTranslatedContent(translatedReviews)) {
                placeWriter.saveGoogleReviews(place, translatedReviews);
            }
        }
    }

    private List<GoogleReview> translateGoogleReviews(List<GoogleReview> reviews) {
        return reviews.stream()
                .map(review -> {
                    String content = review.content();
                    if (!isTranslatable(content)) {
                        return review;
                    }

                    String translated = reviewTranslator.translate(content);
                    return translated != null ? review.withTranslateContent(translated) : review;
                })
                .toList();
    }

    private boolean isTranslatable(String content) {
        return content != null && !content.isBlank() && !TextUtils.isKorean(content);
    }

    private boolean hasTranslatedContent(List<GoogleReview> reviews) {
        return reviews.stream()
                .anyMatch(r -> r.translatedContent() != null);
    }
}
