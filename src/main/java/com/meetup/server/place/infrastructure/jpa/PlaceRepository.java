package com.meetup.server.place.infrastructure.jpa;

import com.meetup.server.place.domain.Place;
import com.meetup.server.place.infrastructure.jpa.projection.PlaceWithDistance;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository extends JpaRepository<Place, UUID> {

    boolean existsByKakaoPlaceId(String kakaoPlaceId);

    Optional<Place> findByGooglePlaceId(String googlePlaceId);

    @Query("""
                SELECT new com.meetup.server.place.infrastructure.jpa.projection.PlaceWithDistance(
                    p,
                    st_distance(p.point, :point)
                )
                FROM Place p
                WHERE p.id = :placeId
            """)
    PlaceWithDistance findPlaceWithDistance(@Param("placeId") UUID placeId, @Param("point") Point point);


    @Query("""
                SELECT new com.meetup.server.place.infrastructure.jpa.projection.PlaceWithDistance(
                    p,
                    st_distance(p.point, :point)
                )
                FROM Place p
                WHERE CAST(st_dwithin(p.point, :point, :radius, true) AS boolean) = true
            """)
    List<PlaceWithDistance> findAllWithinRadius(@Param("point") Point point, @Param("radius") double radius);
}
