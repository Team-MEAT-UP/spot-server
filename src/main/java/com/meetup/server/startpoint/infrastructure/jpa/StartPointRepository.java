package com.meetup.server.startpoint.infrastructure.jpa;

import com.meetup.server.event.domain.Event;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.infrastructure.jpa.projection.RetentionStat;
import com.meetup.server.startpoint.infrastructure.querydsl.StartPointCustomRepository;
import com.meetup.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface StartPointRepository extends JpaRepository<StartPoint, UUID>, StartPointCustomRepository {

    int countByEvent(Event event);

    @Query("SELECT DISTINCT sp FROM StartPoint sp JOIN FETCH sp.event WHERE sp.event = :event")
    List<StartPoint> findAllByEvent(@Param("event") Event event);

    @Query("SELECT DISTINCT sp FROM StartPoint sp LEFT JOIN FETCH sp.user WHERE sp.event = :event")
    List<StartPoint> findAllWithUserByEvent(@Param("event") Event event);

    @Modifying
    @Query("DELETE FROM StartPoint sp WHERE sp.event = :event")
    void deleteAllByEvent(@Param("event") Event event);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM StartPoint sp WHERE sp.user = :user")
    void deleteAllByUser(@Param("user") User user);

    long countByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query(value = """
            SELECT
                date_trunc('day', sp.created_at)::date as date,
                COUNT(DISTINCT sp.user_id) as totalUsers,
                -- 1. 장소 확정 리텐션 유저
                COUNT(DISTINCT ru.user_id) as retainedUsers,
                -- 2. 카카오 유입 + 장소 확정 리텐션 유저
                COUNT(DISTINCT rku.user_id) as retainedUsersWithKakao
            FROM start_point sp
            -- [POOL 1] 장소 확정 2회 이상 경험 유저
            LEFT JOIN (
                SELECT s.user_id
                FROM start_point s
                JOIN event e ON s.event_id = e.event_id
                WHERE s.created_at BETWEEN :thirtyDaysAgo AND :endDateTime
                  AND e.place_id IS NOT NULL
                GROUP BY s.user_id
                HAVING COUNT(DISTINCT s.event_id) >= 2
            ) ru ON sp.user_id = ru.user_id
            -- [POOL 2] 카카오 유입 & 장소 확정 2회 이상 경험 유저
            LEFT JOIN (
                SELECT s.user_id
                FROM start_point s
                JOIN event e ON s.event_id = e.event_id
                JOIN log_event_inflow lei ON e.event_id = lei.event_id
                WHERE s.created_at BETWEEN :thirtyDaysAgo AND :endDateTime
                  AND e.place_id IS NOT NULL
                  AND lei.inflow_type = 'KAKAO'
                GROUP BY s.user_id
                HAVING COUNT(DISTINCT s.event_id) >= 2
            ) rku ON sp.user_id = rku.user_id
            WHERE sp.created_at BETWEEN :startDateTime AND :endDateTime
              AND sp.is_user = true
            GROUP BY date
            ORDER BY date
            """, nativeQuery = true)
    List<RetentionStat> findDailyRetentionStats(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo
    );
}
