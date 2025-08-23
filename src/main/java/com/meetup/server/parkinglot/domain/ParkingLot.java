package com.meetup.server.parkinglot.domain;

import com.meetup.server.startpoint.domain.type.Location;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "parking_lot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parking_lot_id")
    private Integer parkingLotId;

    @Column(name = "parking_lot_name", length = 50, nullable = false)
    private String name;

    @Embedded
    private Location location;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Point point;

    @Builder
    public ParkingLot(String name, Location location, Point point) {
        this.name = name;
        this.location = location;
        this.point = point;
    }
}
