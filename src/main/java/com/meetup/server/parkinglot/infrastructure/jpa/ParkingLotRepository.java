package com.meetup.server.parkinglot.infrastructure.jpa;

import com.meetup.server.parkinglot.domain.ParkingLot;
import com.meetup.server.parkinglot.infrastructure.jpa.projection.ClosestParkingLot;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Integer> {

    @Query("""
                SELECT new com.meetup.server.parkinglot.infrastructure.jpa.projection.ClosestParkingLot(
                    p,
                    st_distance(p.point, :point))
                FROM ParkingLot p
                WHERE st_distance(p.point, :point) = (
                    SELECT MIN(st_distance(p2.point, :point))
                    FROM ParkingLot p2)
            """)
    ClosestParkingLot findClosestParkingLot(@Param("point") Point point);
}
