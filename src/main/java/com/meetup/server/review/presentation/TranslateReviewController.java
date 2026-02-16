package com.meetup.server.review.presentation;

import com.meetup.server.global.support.response.ApiResponse;
import com.meetup.server.review.application.TranslateGoogleReviewService;
import com.meetup.server.review.application.TranslateService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews/translation")
@RequiredArgsConstructor
public class TranslateReviewController {

    private final TranslateService translateService;
    private final TranslateGoogleReviewService translateGoogleReviewService;

    @Hidden
    @PostMapping("")
    public ApiResponse<?> translateAndSaveReview() {
        translateService.saveReviewForKor();
        return ApiResponse.success();
    }

    @Hidden
    @PostMapping("/google")
    public ApiResponse<?> translateAndSaveGoogleReview() {
        translateGoogleReviewService.saveReviewForKor();
        return ApiResponse.success();
    }
}
