package com.meetup.server.review.dto.request;

import com.meetup.server.review.domain.type.NonVisitedReasonCategory;
import lombok.Builder;

import java.util.List;

@Builder
public record NonVisitedReviewRequest(
        List<NonVisitedReasonCategory> categories,
        String etcReason,
        String placeName,
        String address,
        String roadAddress,
        Double longitude,
        Double latitude
) {
}
