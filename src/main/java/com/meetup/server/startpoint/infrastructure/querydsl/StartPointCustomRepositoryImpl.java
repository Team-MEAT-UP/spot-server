package com.meetup.server.startpoint.infrastructure.querydsl;

import com.meetup.server.startpoint.infrastructure.querydsl.projection.EventHistory;
import com.meetup.server.startpoint.infrastructure.querydsl.projection.Participant;
import com.meetup.server.startpoint.infrastructure.querydsl.projection.ParticipantCount;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.meetup.server.event.domain.QEvent.event;
import static com.meetup.server.startpoint.domain.QStartPoint.startPoint;

@Repository
@RequiredArgsConstructor
public class StartPointCustomRepositoryImpl implements StartPointCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<EventHistory> findEventHistories(Long userId, UUID lastViewedEventId, int size) {
        BooleanExpression cursorFilter = cursorFilter(userId, lastViewedEventId);

        return jpaQueryFactory
                .select(Projections.constructor(
                        EventHistory.class,
                        event.eventId,
                        event.place.id,
                        event.eventName,
                        event.eventDateTime,
                        event.subway.name,
                        event.place.name,
                        event.createdAt
                ))
                .from(startPoint)
                .join(startPoint.event, event)
                .leftJoin(event.place)
                .leftJoin(event.subway)
                .where(
                        cursorFilter
                                .and(
                                        startPoint.event.eventId.in(
                                                jpaQueryFactory
                                                        .select(startPoint.event.eventId)
                                                        .from(startPoint)
                                                        .groupBy(startPoint.event.eventId)
                                        )
                                )
                )
                .orderBy(event.createdAt.desc(), event.eventId.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression cursorFilter(Long userId, UUID lastViewedEventId) {
        LocalDateTime lastViewedTime = null;
        BooleanExpression cursor = null;

        if (lastViewedEventId != null) {
            lastViewedTime = jpaQueryFactory
                    .select(event.createdAt)
                    .from(event)
                    .where(event.eventId.eq(lastViewedEventId))
                    .fetchOne();

            if (lastViewedTime != null) {
                cursor = event.createdAt.lt(lastViewedTime)
                        .or(event.createdAt.eq(lastViewedTime).and(event.eventId.lt(lastViewedEventId)));
            }
        }

        BooleanExpression isUser = startPoint.user.userId.eq(userId);
        return (cursor != null) ? isUser.and(cursor) : isUser;
    }

    @Override
    public List<Participant> findParticipantsWithImageUrls(List<UUID> eventIds) {
        return jpaQueryFactory
                .select(Projections.constructor(Participant.class,
                        startPoint.event.eventId,
                        startPoint.user.profileImage))
                .from(startPoint)
                .where(startPoint.event.eventId.in(eventIds))
                .fetch();
    }

    @Override
    public List<ParticipantCount> findParticipantsCounts(List<UUID> eventIds) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        ParticipantCount.class,
                        startPoint.event.eventId,
                        startPoint.count()
                ))
                .from(startPoint)
                .where(startPoint.event.eventId.in(eventIds))
                .groupBy(startPoint.event.eventId)
                .fetch();
    }
}
