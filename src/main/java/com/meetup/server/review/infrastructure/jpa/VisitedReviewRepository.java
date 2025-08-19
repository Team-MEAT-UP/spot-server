package com.meetup.server.review.infrastructure.jpa;

import com.meetup.server.review.domain.VisitedReview;
import com.meetup.server.review.infrastructure.jpa.projection.PlaceQuietnessWithRating;
import com.meetup.server.review.infrastructure.jpa.projection.PlaceWithRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VisitedReviewRepository extends JpaRepository<VisitedReview, Long> {

    @Query("""
                SELECT new com.meetup.server.review.infrastructure.jpa.projection.PlaceWithRating(
                    vr.review.place,
                    AVG(vr.placeScore.socket),
                    AVG(vr.placeScore.seat),
                    AVG(vr.placeScore.quiet)
                )
                FROM VisitedReview vr
                WHERE vr.review.place.id IN :placeIds
                GROUP BY vr.review.place
            """)
    List<PlaceWithRating> findPlaceRatingsByPlaceIds(@Param("placeIds") List<UUID> placeIds);


    @Query("""
                SELECT new com.meetup.server.review.infrastructure.jpa.projection.PlaceQuietnessWithRating(
                    AVG(CASE WHEN vr.visitedTime = 'MORNING' THEN vr.placeScore.quiet ELSE NULL END),
                    AVG(CASE WHEN vr.visitedTime = 'LUNCH' THEN vr.placeScore.quiet ELSE NULL END),
                    AVG(CASE WHEN vr.visitedTime = 'NIGHT' THEN vr.placeScore.quiet ELSE NULL END)
                )
                FROM VisitedReview vr
                WHERE vr.review.place.id = :placeId
            """)
    PlaceQuietnessWithRating findQuietnessRatingByPlaceId(@Param("placeId") UUID placeId);
}
