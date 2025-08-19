package com.meetup.server.event.infrastructure.jpa;

import com.meetup.server.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
