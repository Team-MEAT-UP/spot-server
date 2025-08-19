package com.meetup.server.review.infrastructure.querydsl;

import com.meetup.server.startpoint.infrastructure.querydsl.projection.EventHistory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReviewCustomRepository {
    Map<UUID, Boolean> findReviewsWrittenByUser(List<EventHistory> projections, Long userId);
}
