package com.meetup.server.startpoint.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import com.meetup.server.event.domain.Event;
import com.meetup.server.global.domain.BaseEntity;
import com.meetup.server.startpoint.domain.type.Address;
import com.meetup.server.startpoint.domain.type.Location;
import com.meetup.server.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "start_point", uniqueConstraints = {@UniqueConstraint(columnNames = {"event_id", "user_id"})})
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class StartPoint extends BaseEntity {

    @Id
    @Column(name = "start_point_id")
    private UUID startPointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "start_point_name", length = 50, nullable = false)
    private String name;

    @Column(name = "is_user", nullable = false)
    private boolean isUser;

    @Column(name = "is_transit", nullable = false)
    private boolean isTransit;

    @Column(name = "non_user_name", length = 5)
    private String nonUserName;

    @Embedded
    private Address address;

    @Embedded
    private Location location;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Point point;

    @Column(name = "guest_id")
    private UUID guestId;

    @PrePersist
    public void prePersist() {
        this.startPointId = UuidCreator.getTimeOrderedEpoch();

        if (!isUser && this.guestId == null) {
            this.guestId = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Builder
    public StartPoint(UUID startPointId, Event event, User user, String name, Address address, Location location, String nonUserName, Point point, boolean isUser, UUID guestId, boolean isTransit) {
        this.startPointId = startPointId;
        this.event = event;
        this.user = user;
        this.name = name;
        this.address = address;
        this.location = location;
        this.nonUserName = nonUserName;
        this.point = point;
        this.isUser = isUser;
        this.guestId = guestId;
        this.isTransit = isTransit;
    }

    public boolean getIsUser() {
        return isUser;
    }

    public void updateStartPoint(String name, Address address, Location location, String nonUserName, Point point, boolean isTransit) {
        this.name = name;
        this.address = address;
        this.location = location;
        this.nonUserName = nonUserName;
        this.point = point;
        this.isTransit = isTransit;
    }
}
