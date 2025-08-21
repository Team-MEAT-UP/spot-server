package com.meetup.server.global.email.domain;

import com.meetup.server.event.domain.Event;
import com.meetup.server.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_send_history", uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"}))
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class EmailSendHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false)
    private EmailType emailType;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_send_status", nullable = false)
    private EmailSendStatus emailSendStatus;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Builder
    public EmailSendHistory(Event event, User user, EmailType emailType, EmailSendStatus emailSendStatus, LocalDateTime sentAt, String errorMessage) {
        this.event = event;
        this.user = user;
        this.emailType = emailType;
        this.emailSendStatus = emailSendStatus;
        this.sentAt = sentAt;
        this.errorMessage = errorMessage;
    }
}
