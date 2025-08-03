package com.meetup.server.event.dto.response;

import com.meetup.server.event.domain.Event;
import com.meetup.server.global.util.TimeUtil;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.util.UsernameExtractor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Comparator;
import java.util.List;

public record MeetingPointRoutesResponse(
        @Schema(description = "모임명", example = "SPOT 정기회의")
        String eventName,

        @Schema(description = "모임 날짜 (yyyy-MM-dd 형식)", example = "2025-07-09")
        String eventDate,

        @Schema(description = "모임 시간 (HH:mm 형식)", example = "14:00")
        String eventTime,

        @Schema(description = "모임 생성자", example = "홍길동")
        String eventMaker,

        @Schema(description = "확정 장소명", example = "스타벅스 방배카페거리점")
        String placeName,

        @Schema(description = "참여자 수", example = "5")
        int peopleCount,

        @Schema(description = "모임 경로 관련 그룹 데이터")
        List<MeetingPointRouteGroup> meetingPointRouteGroups
) {

    public static MeetingPointRoutesResponse of(List<MeetingPointResult> meetingPointResults, List<MeetingPointRouteGroup> meetingPointRouteGroups) {
        MeetingPointResult meetingPointResult = meetingPointResults.getFirst();
        Event event = meetingPointResult.event();

        return new MeetingPointRoutesResponse(
                event.getEventName(),
                TimeUtil.formatAsDashDate(event.getEventDateTime()),
                TimeUtil.formatAsTime(event.getEventDateTime()),
                extractEventMaker(meetingPointResult.startPoints()),
                extractPlaceName(event),
                meetingPointResult.startPoints().size(),
                meetingPointRouteGroups
        );
    }

    private static String extractPlaceName(Event event) {
        return event.getPlace() != null ? event.getPlace().getName() : null;
    }

    private static String extractEventMaker(List<StartPoint> startPoints) {
        return startPoints.stream()
                .min(Comparator.comparing(StartPoint::getCreatedAt))
                .map(UsernameExtractor::extractDisplayName)
                .orElse(null);
    }

    public MeetingPointRoutesResponse withPlaceName(String confirmedPlaceName) {
        return new MeetingPointRoutesResponse(
                this.eventName,
                this.eventDate,
                this.eventTime,
                this.eventMaker,
                confirmedPlaceName,
                this.peopleCount,
                this.meetingPointRouteGroups
        );
    }
}
