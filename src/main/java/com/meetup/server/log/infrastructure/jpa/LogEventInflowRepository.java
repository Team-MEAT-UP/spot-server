package com.meetup.server.log.infrastructure.jpa;

import com.meetup.server.log.domain.LogEventInflow;
import com.meetup.server.log.domain.type.InflowType;
import com.meetup.server.log.infrastructure.jpa.projection.EventInflowCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LogEventInflowRepository extends JpaRepository<LogEventInflow, Long> {

    long countByInflowTypeAndCreatedAtBetween(InflowType inflowType, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("""
                    SELECT new com.meetup.server.log.infrastructure.jpa.projection.EventInflowCount(l.eventId, COUNT(l))
                    FROM LogEventInflow l
                    WHERE l.inflowType = :inflowType AND l.eventId IN :eventIds
                    GROUP BY l.eventId
            """)
    List<EventInflowCount> findCountsByInflowTypeAndEventIds(InflowType inflowType, List<UUID> eventIds);
}
