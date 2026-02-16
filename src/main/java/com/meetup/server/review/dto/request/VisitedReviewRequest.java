package com.meetup.server.review.dto.request;

import com.meetup.server.review.domain.type.VisitedTime;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record VisitedReviewRequest(

        @NotNull
        VisitedTime visitedTime,

        @Min(value = 1) @Max(value = 5)
        int socket,

        @Min(value = 1) @Max(value = 5)
        int seat,

        @Min(value = 1) @Max(value = 5)
        int quiet,

        String content
) {
}
