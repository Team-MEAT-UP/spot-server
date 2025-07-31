package com.meetup.server.review.domain;

import com.meetup.server.event.domain.Event;
import com.meetup.server.global.domain.BaseEntity;
import com.meetup.server.place.domain.Place;
import com.meetup.server.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = true)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_visited", nullable = false)
    private boolean isVisited;

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private VisitedReview visitedReview;

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private NonVisitedReview nonVisitedReview;

    @Builder
    public Review(Place place, User user, boolean isVisited, VisitedReview visitedReview, NonVisitedReview nonVisitedReview, Event event) {
        this.place = place;
        this.user = user;
        this.isVisited = isVisited;
        this.visitedReview = visitedReview;
        this.nonVisitedReview = nonVisitedReview;
        this.event = event;
    }

    public boolean isWrittenBy(Long userId) {
        return this.user.getUserId().equals(userId);
    }

    public void addVisitedReview(VisitedReview visitedReview) {
        this.visitedReview = visitedReview;
        if (visitedReview.getReview() != this) {
            visitedReview.assignTo(this);
        }
    }

    public void addNotVisitedReview(NonVisitedReview nonVisitedReview) {
        this.nonVisitedReview = nonVisitedReview;
        if (nonVisitedReview.getReview() != this) {
            nonVisitedReview.assignTo(this);
        }
    }
}
