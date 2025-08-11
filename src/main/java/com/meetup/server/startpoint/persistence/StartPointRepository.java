package com.meetup.server.startpoint.persistence;

import com.meetup.server.event.domain.Event;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StartPointRepository extends JpaRepository<StartPoint, UUID>, StartPointCustomRepository {

    int countByEvent(Event event);

    @Query("SELECT DISTINCT sp FROM StartPoint sp JOIN FETCH sp.event WHERE sp.event = :event")
    List<StartPoint> findAllByEvent(@Param("event") Event event);

    @Modifying
    @Query("DELETE FROM StartPoint sp WHERE sp.event = :event")
    void deleteAllByEvent(@Param("event") Event event);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM StartPoint sp WHERE sp.user = :user")
    void deleteAllByUser(@Param("user") User user);
}
