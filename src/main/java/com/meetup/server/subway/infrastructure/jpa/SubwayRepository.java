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
                WHERE ST_Distance(s.point, :startPoint) = (
                    SELECT MIN(ST_Distance(s2.point, :startPoint))
                    FROM subway s2
                )
                LIMIT 1
            """, nativeQuery = true)
    Subway findClosestSubway(@Param("startPoint") Point startPoint);

    @Query("""
                SELECT s
                FROM Subway s
                WHERE CAST(st_dwithin(s.point, :centerPoint, :radius, true) AS boolean) = true
            """)
    List<Subway> findAllWithinRadius(@Param("centerPoint") Point centerPoint, @Param("radius") double radius);

    @Query(value = "SELECT s.* FROM subway s WHERE subway_id IN (:subwayIds) ORDER BY array_position(:subwayIds, subway_id)", nativeQuery = true)
    List<Subway> findAllBySubwayIdInOrderByIds(@Param("subwayIds") Integer[] subwayIds);
}
