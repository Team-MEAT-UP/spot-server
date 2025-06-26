package com.meetup.server.review.application;

import com.meetup.server.global.clients.clova.ClovaClient;
import com.meetup.server.global.clients.clova.ClovaRequest;
import com.meetup.server.global.clients.clova.ClovaResponse;
import com.meetup.server.review.domain.Review;
import com.meetup.server.review.domain.VisitedReview;
import com.meetup.server.review.implement.ReviewReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService {

    private final ClovaClient clovaClient;
    private final ReviewReader reviewReader;

    @Transactional
    public void saveReviewForKor() {
        List<Review> reviews = reviewReader.readAll();
        List<String> filteredForeignReviews = filterForeignReviews(reviews);

        if (filteredForeignReviews.isEmpty()) return;

        for (Review review : reviews) {
            VisitedReview visitedReview = review.getVisitedReview();
            String reviewContent = visitedReview.getContent();

            if (reviewContent == null || reviewContent.isBlank() || !isForeignLang(reviewContent)) {
                continue;
            }

            ClovaRequest request = generateFinalPrompt(reviewContent);
            log.info("[Clova Studio] Request: {}", request);
            ClovaResponse clovaResponse = clovaClient.sendRequest(request);
            log.info("[Clova Studio] response: {}", clovaResponse);

            if (clovaResponse == null || clovaResponse.result() == null || clovaResponse.result().message() == null) {
                log.warn("[Clova Studio] Empty or null response for review: {}", reviewContent);
                continue;
            }

            String translatedContent = clovaResponse.result().message().content();
            log.info("[Clova Studio] Original Content : {}, [Clova Studio] Translated Content: {}", reviewContent, translatedContent);

            visitedReview.updateContent(translatedContent);
        }
    }

    private List<String> filterForeignReviews(List<Review> reviews) {
        List<String> foreignReviews = new ArrayList<>();
        for (Review review: reviews) {
            String content = review.getVisitedReview().getContent();

            if (isForeignLang(content)) {
                foreignReviews.add(content);
            }
        }
        return foreignReviews;
    }

    private ClovaRequest generateFinalPrompt(String review) {
        List<ClovaRequest.Message> messages = new ArrayList<>();
        messages.add(generateSystemPrompt());

        messages.add(ClovaRequest.Message.builder()
                .role("user")
                .content(review)
                .build());

        return ClovaRequest.builder()
                .messages(messages)
                .topP(0.8)
                .topK(0)
                .maxTokens(1000)
                .temperature(0.8)
                .repeatPenalty(5.0)
                .includeAiFilters(false)
                .build();
    }

    private ClovaRequest.Message generateSystemPrompt() {
        return ClovaRequest.Message.builder()
                .role("system")
                .content("""
                        당신은 한국어 번역가입니다. 다음 지침을 따르세요:
                        - 입력하는 문장을 한국어로 번역합니다.
                        - 질문에 대해 답변하지 말고, 질문을 한국어로 번역합니다.
                        - 내용을 변경하지 않고 그대로 번역합니다.
                        """)
                .build();
    }

    private boolean isForeignLang(String content) {
        if (content == null || content.isBlank()) return false;

        String koreanRegex = ".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*";
        return !content.matches(koreanRegex);
    }
}
