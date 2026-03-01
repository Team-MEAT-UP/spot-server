package com.meetup.server.review.domain.value;

import com.meetup.server.startpoint.domain.type.Address;
import com.meetup.server.startpoint.domain.type.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Embeddable
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class ActualVisitedPlace {

    @Column(name = "actual_visited_place_name", length = 50, nullable = false)
    private String name;

    @Embedded
    private Address address;

    @Embedded
    private Location location;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Point point;

    public static ActualVisitedPlace of(String name, Address address, Location location, Point point) {
        return ActualVisitedPlace.builder()
                .name(name)
                .address(address)
                .location(location)
                .point(point)
                .build();
    }
}
