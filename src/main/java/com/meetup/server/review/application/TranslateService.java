package com.meetup.server.review.application;

import com.meetup.server.review.domain.Review;
import com.meetup.server.review.domain.VisitedReview;
import com.meetup.server.review.implement.ReviewReader;
import com.meetup.server.review.implement.ReviewTranslator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService {

    private final ReviewReader reviewReader;
    private final ReviewTranslator reviewTranslator;

    @Transactional
    public void saveReviewForKor() {
        List<Review> reviews = reviewReader.readAll();

        for (Review review : reviews) {
            VisitedReview visitedReview = review.getVisitedReview();
            String content = visitedReview.getContent();

            if (!isTranslatable(content)) continue;

            String translated = reviewTranslator.translate(content);
            if (translated != null) {
                visitedReview.updateContent(translated);
            }
        }
    }

    private boolean isTranslatable(String content) {
        return content != null && !content.isBlank() && !isKorean(content);
    }

    private boolean isKorean(String text) {
        return text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
    }
}
