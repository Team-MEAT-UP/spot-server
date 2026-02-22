package com.meetup.server.event.infrastructure.jpa;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.infrastructure.jpa.projection.ActivationStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Modifying
    @Query("UPDATE Event e SET e.routes = null WHERE e.eventId = :eventId")
    void deleteRoutesByEventId(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE Event e SET e.routes = :routes WHERE e.eventId = :eventId")
    void saveRoutesByEventId(@Param("eventId") UUID eventId,
                             @Param("routes") MeetingPointRouteGroups routes);

    @Query("SELECT e FROM Event e JOIN FETCH e.place WHERE e.eventDateTime = :eventDateTime")
    List<Event> findAllByEventDateTimeWithPlace(@Param("eventDateTime") LocalDateTime eventDateTime);

    long countByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query(value = """
            SELECT
                e.created_at::date as date,
                COUNT(*) as total_events,
                COUNT(e.place_id) as confirmed_events,
                COUNT(CASE WHEN e.place_id IS NOT NULL AND EXISTS (
                    SELECT 1 FROM log_event_inflow l WHERE l.event_id = e.event_id AND l.inflow_type = 'KAKAO'
                ) THEN 1 END) as confirmed_with_kakao
            FROM event e
            WHERE e.created_at BETWEEN :startDateTime AND :endDateTime
            GROUP BY date
            ORDER BY date
            """, nativeQuery = true)
    List<ActivationStat> findDailyActivationStats(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
