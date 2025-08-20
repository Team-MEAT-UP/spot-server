package com.meetup.server.event.infrastructure.jpa;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Modifying
    @Query("UPDATE Event e SET e.route = null WHERE e.eventId = :eventId")
    void deleteRouteByEventId(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE Event e SET e.route = :route WHERE e.eventId = :eventId")
    void saveRouteByEventId(@Param("eventId") UUID eventId,
                              @Param("route") MeetingPointRouteGroups route);
}
