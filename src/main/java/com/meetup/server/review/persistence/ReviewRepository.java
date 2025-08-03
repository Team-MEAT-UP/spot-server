package com.meetup.server.review.persistence;

import com.meetup.server.event.domain.Event;
import com.meetup.server.place.domain.Place;
import com.meetup.server.review.domain.Review;
import com.meetup.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewCustomRepository {

    List<Review> findAllByPlace(Place place);

    boolean existsByEventAndPlaceAndUser(Event event, Place place, User user);

    @Modifying
    @Query("UPDATE Review r SET r.event = null WHERE r.event = :event")
    void updateEventToNull(@Param("event") Event event);
}
