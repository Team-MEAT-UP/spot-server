package com.meetup.server.subway.infrastructure.jpa;

import com.meetup.server.subway.domain.Subway;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubwayRepository extends JpaRepository<Subway, Integer> {

    @Query(value = """
                SELECT s.*
                FROM subway s
                ORDER BY s.point <-> :startPoint
                LIMIT 1
            """, nativeQuery = true)
    Subway findClosestSubway(@Param("startPoint") Point startPoint);

    @Query("""
                SELECT s
                FROM Subway s
                WHERE CAST(st_dwithin(s.point, :centerPoint, :radius, true) AS boolean) = true
            """)
    List<Subway> findAllWithinRadius(@Param("centerPoint") Point centerPoint, @Param("radius") double radius);
}
