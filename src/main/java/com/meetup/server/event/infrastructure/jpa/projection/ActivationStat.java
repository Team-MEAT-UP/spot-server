package com.meetup.server.event.infrastructure.jpa.projection;

import java.time.LocalDate;

public interface ActivationStat {
    LocalDate getDate();
    Long getTotalEvents();
    Long getConfirmedEvents();
    Long getConfirmedWithKakao();
}
