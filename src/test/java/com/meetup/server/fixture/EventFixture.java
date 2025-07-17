package com.meetup.server.fixture;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.request.EventRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventFixture {

    public static Event getEvent() {
        return Event.builder()
                .eventName("모임핑")
                .eventDateTime(LocalDateTime.parse("2025-10-01T10:00:00"))
                .build();
    }

    public static EventRequest getEventRequest() {
        return new EventRequest(
                "모임핑",
                LocalDate.parse("2025-10-01"),
                LocalTime.parse("10:00"),
                "땡수팟",
                "선정릉역 수인분당선",
                "서울특별시 강남구 삼성동 111-114",
                "서울특별시 강남구 선릉로 지하580",
                127.043999,
                37.510297
        );
    }
}
