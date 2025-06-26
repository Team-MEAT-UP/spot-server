package com.meetup.server.review.domain;

import com.meetup.server.global.domain.BaseEntity;
import com.meetup.server.review.domain.type.VisitedTime;
import com.meetup.server.review.domain.value.PlaceScore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visited_review")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class VisitedReview extends BaseEntity {

    @Id
    @Column(name = "review_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "review_id")
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(name = "visited_time", nullable = false)
    private VisitedTime visitedTime;

    @Embedded
    private PlaceScore placeScore;

    @Column(name = "content")
    private String content;

    @Builder
    public VisitedReview(Review review, VisitedTime visitedTime, PlaceScore placeScore, String content) {
        this.review = review;
        this.visitedTime = visitedTime;
        this.placeScore = placeScore;
        this.content = content;
    }

    public void assignTo(Review review) {
        this.review = review;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
