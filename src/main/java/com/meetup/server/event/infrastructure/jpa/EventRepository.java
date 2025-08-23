package com.meetup.server.event.infrastructure.jpa;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
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
}
