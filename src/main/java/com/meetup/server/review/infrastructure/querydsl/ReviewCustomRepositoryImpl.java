package com.meetup.server.review.infrastructure.querydsl;

import com.meetup.server.startpoint.infrastructure.querydsl.projection.EventHistory;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.meetup.server.review.domain.QReview.review;

@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Map<UUID, Boolean> findReviewsWrittenByUser(List<EventHistory> projections, Long userId) {
        Map<UUID, UUID> placeOfEventMap = projections.stream()
                .filter(projection -> projection.placeId() != null)
                .collect(Collectors.toMap(EventHistory::eventId, EventHistory::placeId));

        List<Tuple> reviewTuples = jpaQueryFactory
                .select(review.event.eventId, review.place.id)
                .from(review)
                .where(
                        review.place.id.in(placeOfEventMap.values()),
                        review.user.userId.eq(userId),
                        review.event.eventId.in(placeOfEventMap.keySet())
                )
                .fetch();

        Set<UUID> reviewedEventIds = reviewTuples.stream()
                .filter(tuple -> {
                    UUID eventId = tuple.get(review.event.eventId);
                    UUID placeId = tuple.get(review.place.id);
                    return placeId != null && placeId.equals(placeOfEventMap.get(eventId));
                })
                .map(tuple -> tuple.get(review.event.eventId))
                .collect(Collectors.toSet());

        return placeOfEventMap.keySet().stream()
                .collect(Collectors.toMap(
                        eventId -> eventId,
                        reviewedEventIds::contains
                ));
    }
}
