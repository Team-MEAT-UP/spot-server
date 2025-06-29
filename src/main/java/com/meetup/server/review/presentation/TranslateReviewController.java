package com.meetup.server.review.presentation;

import com.meetup.server.global.support.response.ApiResponse;
import com.meetup.server.review.application.TranslateGoogleReviewService;
import com.meetup.server.review.application.TranslateService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review API", description = "리뷰 API")
@RestController
@RequestMapping("/reviews/translation")
@RequiredArgsConstructor
public class TranslateReviewController {

    private final TranslateService translateService;
    private final TranslateGoogleReviewService translateGoogleReviewService;

    @Hidden
    @Operation(summary = "자체 리뷰 번역 API", description = "외국어로 작성된 리뷰를 한국어로 번역하여 저장합니다")
    @PostMapping("")
    public ApiResponse<?> translateAndSaveReview(){
        translateService.saveReviewForKor();
        return ApiResponse.success();
    }

    @Hidden
    @Operation(summary = "구글 리뷰 번역 API", description = "장소의 구글 리뷰를 한국어로 번역하여 저장합니다")
    @PostMapping("/google")
    public ApiResponse<?> translateAndSaveGoogleReview(){
        translateGoogleReviewService.saveReviewForKor();
        return ApiResponse.success();
    }
}
