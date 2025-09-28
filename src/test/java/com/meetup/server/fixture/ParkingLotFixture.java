package com.meetup.server.fixture;

import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.parkinglot.domain.ParkingLot;
import com.meetup.server.startpoint.domain.type.Location;

public class ParkingLotFixture {

    public static ParkingLot getParkingLot() {
        return ParkingLot.builder()
                .name("역삼문화공원 공영주차장")
                .location(Location.of(127.03006081, 37.5026327))
                .point(CoordinateUtil.createPoint(127.03006081, 37.5026327))
                .build();
    }
}
