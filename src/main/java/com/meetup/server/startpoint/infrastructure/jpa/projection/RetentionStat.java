package com.meetup.server.startpoint.infrastructure.jpa.projection;

import java.time.LocalDate;

public interface RetentionStat {
    LocalDate getDate();
    Long getTotalUsers();
    Long getRetainedUsers();
    Long getRetainedUsersWithKakao();
}
