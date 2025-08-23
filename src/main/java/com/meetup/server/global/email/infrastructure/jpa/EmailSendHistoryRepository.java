package com.meetup.server.global.email.infrastructure.jpa;

import com.meetup.server.event.domain.Event;
import com.meetup.server.global.email.domain.EmailSendHistory;
import com.meetup.server.global.email.domain.EmailType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailSendHistoryRepository extends JpaRepository<EmailSendHistory, Long> {

    List<EmailSendHistory> findAllByEventInAndEmailType(List<Event> events, EmailType emailType);
}
