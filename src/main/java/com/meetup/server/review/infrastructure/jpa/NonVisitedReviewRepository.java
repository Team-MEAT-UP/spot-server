package com.meetup.server.review.infrastructure.jpa;

import com.meetup.server.review.domain.NonVisitedReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NonVisitedReviewRepository extends JpaRepository<NonVisitedReview, Long> {
}
