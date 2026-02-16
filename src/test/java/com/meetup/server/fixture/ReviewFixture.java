package com.meetup.server.fixture;

import com.meetup.server.review.domain.type.NonVisitedReasonCategory;
import com.meetup.server.review.domain.type.VisitedTime;
import com.meetup.server.review.dto.request.NonVisitedReviewRequest;
import com.meetup.server.review.dto.request.VisitedReviewRequest;

import java.util.List;

public class ReviewFixture {

    public static final String VISITED_REVIEW_CONTENT = "카공하기 좋은 느좋카페";
    public static final String NON_VISITED_REASON = "다른 카페 가고 싶어요";
    public static final String PLACE_NAME = "선정릉역 수인분당선";
    public static final String ADDRESS = "서울특별시 강남구 삼성동 111-114";
    public static final String ROAD_ADDRESS = "서울특별시 강남구 선릉로 지하580";
    public static final double LONGITUDE = 127.043999;
    public static final double LATITUDE = 37.510297;

    public static VisitedReviewRequest getVisitedReviewRequest() {
        return VisitedReviewRequest.builder()
                .visitedTime(VisitedTime.MORNING)
                .socket(4)
                .seat(5)
                .quiet(3)
                .content(VISITED_REVIEW_CONTENT)
                .build();
    }

    public static NonVisitedReviewRequest getNonVisitedReviewRequest() {
        return NonVisitedReviewRequest.builder()
                .categories(List.of(NonVisitedReasonCategory.NOISY, NonVisitedReasonCategory.CONGESTION))
                .etcReason(NON_VISITED_REASON)
                .placeName(PLACE_NAME)
                .address(ADDRESS)
                .roadAddress(ROAD_ADDRESS)
                .longitude(LONGITUDE)
                .latitude(LATITUDE)
                .build();
    }
}
