package com.meetup.server.event.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import com.meetup.server.global.domain.BaseEntity;
import com.meetup.server.place.domain.Place;
import com.meetup.server.subway.domain.Subway;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class Event extends BaseEntity {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_date_time", nullable = false)
    private LocalDateTime eventDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subway_id", nullable = true)
    private Subway subway;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = true)
    private Place place;

    @PrePersist
    public void prePersist() {
        this.eventId = UuidCreator.getTimeOrderedEpoch();
    }

    @Builder
    public Event(Subway subway, Place place, String eventName, LocalDateTime eventDateTime) {
        this.subway = subway;
        this.place = place;
        this.eventName = eventName;
        this.eventDateTime = eventDateTime;
    }

    public void updateMeetingPlace(Place place, Subway subway) {
        this.place = place;
        this.subway = subway;
    }
}
