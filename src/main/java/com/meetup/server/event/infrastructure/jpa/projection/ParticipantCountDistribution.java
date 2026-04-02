package com.meetup.server.event.infrastructure.jpa.projection;

public interface ParticipantCountDistribution {
    Long getParticipantCount();
    Long getEventCount();
}
