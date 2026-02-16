package com.meetup.server.review.presentation;

import com.meetup.server.global.support.response.ApiResponse;
import com.meetup.server.review.application.ReviewService;
import com.meetup.server.review.dto.request.NonVisitedReviewRequest;
import com.meetup.server.review.dto.request.VisitedReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/places/{placeId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/visited")
    public ApiResponse<?> createVisitedReview(
            @PathVariable("placeId") UUID placeId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "eventId") UUID eventId,
            @Valid @RequestBody VisitedReviewRequest visitedReviewRequest) {

        reviewService.createVisitedReview(eventId, placeId, userId, visitedReviewRequest);
        return ApiResponse.success();
    }

    @PostMapping("/non-visited")
    public ApiResponse<?> createNonVisitedReview(
            @PathVariable("placeId") UUID placeId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "eventId") UUID eventId,
            @Valid @RequestBody NonVisitedReviewRequest nonVisitedReviewRequest) {

        reviewService.createNonVisitedReview(eventId, placeId, userId, nonVisitedReviewRequest);
        return ApiResponse.success();
    }
}
