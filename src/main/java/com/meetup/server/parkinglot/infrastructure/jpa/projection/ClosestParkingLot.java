package com.meetup.server.parkinglot.infrastructure.jpa.projection;

import com.meetup.server.parkinglot.domain.ParkingLot;

public record ClosestParkingLot(
        ParkingLot parkingLot,
        double distance
) {
}
