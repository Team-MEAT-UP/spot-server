package com.meetup.server.review.implement;

import com.meetup.server.event.domain.Event;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.place.domain.Place;
import com.meetup.server.review.domain.NonVisitedReview;
import com.meetup.server.review.domain.Review;
import com.meetup.server.review.domain.VisitedReview;
import com.meetup.server.review.domain.value.ActualVisitedPlace;
import com.meetup.server.review.domain.value.PlaceScore;
import com.meetup.server.review.dto.request.NonVisitedReviewRequest;
import com.meetup.server.review.dto.request.VisitedReviewRequest;
import com.meetup.server.review.persistence.ReviewRepository;
import com.meetup.server.startpoint.domain.type.Address;
import com.meetup.server.startpoint.domain.type.Location;
import com.meetup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewWriter {

    private final ReviewRepository reviewRepository;
    private final ReviewValidator reviewValidator;

    public void saveVisitedReview(Event event, Place place, User user, VisitedReviewRequest visitedReviewRequest) {
        reviewValidator.validateReviewIsAlreadyWritten(event, place, user);

        Review review = createReview(event, place, user, true);

        VisitedReview visitedReview = VisitedReview.builder()
                .review(review)
                .visitedTime(visitedReviewRequest.visitedTime())
                .placeScore(PlaceScore.of(visitedReviewRequest.socket(), visitedReviewRequest.seat(), visitedReviewRequest.quiet()))
                .content(visitedReviewRequest.content())
                .build();

        review.addVisitedReview(visitedReview);
        reviewRepository.save(review);
    }

    public void saveNonVisitedReview(Event event, Place place, User user, NonVisitedReviewRequest nonVisitedReviewRequest) {
        reviewValidator.validateReviewIsAlreadyWritten(event, place, user);

        Review review = createReview(event, place, user, false);

        NonVisitedReview.NonVisitedReviewBuilder nonVisitedReviewBuilder = NonVisitedReview.builder()
                .review(review)
                .categories(nonVisitedReviewRequest.categories())
                .etcReason(nonVisitedReviewRequest.etcReason());

        if (nonVisitedReviewRequest.placeName() != null) {
            double longitude = nonVisitedReviewRequest.longitude();
            double latitude = nonVisitedReviewRequest.latitude();

            nonVisitedReviewBuilder.
                    actualVisitedPlace(ActualVisitedPlace.of(
                            nonVisitedReviewRequest.placeName(),
                            Address.of(nonVisitedReviewRequest.address(), nonVisitedReviewRequest.roadAddress()),
                            Location.of(longitude, latitude),
                            CoordinateUtil.createPoint(longitude, latitude)
                    ));
        }

        review.addNotVisitedReview(nonVisitedReviewBuilder.build());
        reviewRepository.save(review);
    }

    private Review createReview(Event event, Place place, User user, boolean isVisited) {
        return Review.builder()
                .event(event)
                .place(place)
                .user(user)
                .isVisited(isVisited)
                .build();
    }

    public void unlinkFromEvent(Event event) {
        reviewRepository.updateEventToNull(event);
    }
}
