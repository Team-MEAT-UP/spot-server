package com.meetup.server.user.dto.response;

import com.meetup.server.global.util.TimeUtil;
import com.meetup.server.startpoint.persistence.projection.EventHistory;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record UserEventHistoryResponse(
        UUID eventId,
        String eventName,
        String eventDate,
        String eventTime,
        String middlePointName,
        String placeName,
        int participatedPeopleCount,
        List<String> userProfileImageUrls,
        int eventMadeAgo,
        int eventHourAgo,
        boolean isReviewed
) {
    public static UserEventHistoryResponse of(EventHistory eventHistory, int participatedPeopleCount, List<String> imageUrls, boolean isReviewed) {
        return UserEventHistoryResponse.builder()
                .eventId(eventHistory.eventId())
                .eventName(eventHistory.eventName())
                .eventDate(TimeUtil.formatAsDashDate(eventHistory.eventDateTime()))
                .eventTime(TimeUtil.formatAsTime(eventHistory.eventDateTime()))
                .middlePointName(eventHistory.subwayName())
                .placeName(eventHistory.placeName())
                .participatedPeopleCount(participatedPeopleCount)
                .userProfileImageUrls(imageUrls)
                .eventMadeAgo(TimeUtil.calculateDaysAgo(eventHistory.createdAt()))
                .eventHourAgo(TimeUtil.calculateHoursAgo(eventHistory.createdAt()))
                .isReviewed(isReviewed)
                .build();
    }
}
