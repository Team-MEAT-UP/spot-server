package com.meetup.server.startpoint.persistence;

import com.meetup.server.event.domain.Event;
import com.meetup.server.startpoint.domain.StartPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StartPointRepository extends JpaRepository<StartPoint, UUID>, StartPointCustomRepository {

    int countByEvent(Event event);

    @Query("SELECT DISTINCT s FROM StartPoint s JOIN FETCH s.event WHERE s.event = :event")
    List<StartPoint> findAllByEvent(@Param("event") Event event);
}
