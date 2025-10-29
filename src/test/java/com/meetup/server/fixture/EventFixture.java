package com.meetup.server.fixture;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.request.EventRequest;
import com.meetup.server.event.dto.request.UpdateEventRequest;
import com.meetup.server.event.dto.request.UpdatePlaceRequest;
import com.meetup.server.event.dto.response.route.*;
import com.meetup.server.parkinglot.dto.response.ParkingLotResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class EventFixture {

    public static final UUID EVENT_ID = UUID.fromString("01968fe2-5277-712a-ad3c-98f29c2782e0");

    public static Event getEvent() {
        return Event.builder()
                .eventId(EVENT_ID)
                .eventName("모임핑")
                .eventDateTime(LocalDateTime.parse("2025-10-01T10:00:00"))
                .build();
    }

    public static UpdateEventRequest getUpdateEventRequest() {
        return new UpdateEventRequest(
                "수정핑",
                LocalDate.parse("2025-08-01"),
                LocalTime.parse("12:00")
        );
    }

    public static UpdatePlaceRequest getUpdatePlaceRequest() {
        return new UpdatePlaceRequest(
                UUID.fromString("0196f346-5244-79b9-85b0-955d6328f09b"),
                1
        );
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
                37.510297,
                true
        );
    }

    public static Event getEventWithRoute() {
        return Event.builder()
                .eventName("모임핑")
                .eventDateTime(LocalDateTime.parse("2025-10-01T10:00:00"))
                .routes(getMeetingPointRouteGroups())
                .place(PlaceFixture.getPlace())
                .build();
    }

    public static MeetingPointRouteGroups getMeetingPointRouteGroups() {
        List<TransitRouteResponse> transitRoutes = List.of(RouteFixture.getTransitRoute());
        DrivingInfoResponse drivingInfo = RouteFixture.getDrivingInfo();
        List<DrivingRouteResponse> drivingRoutes = List.of(RouteFixture.getDrivingRoute());

        List<RouteResponse> routeResponses = List.of(
                new RouteResponse(true, false, UUID.fromString("0198c64a-5a06-7095-a692-3c5e1a2c294f"),
                        null, UUID.fromString("0198c64a-5a06-7095-a693-c3f8ebde622c"), "김아무개",
                        null, "강남구 삼성동", 127.043999, 37.510297, transitRoutes, null, null, 10),
                new RouteResponse(false, false, UUID.fromString("0198c64b-086a-796d-a4d3-105c89ee529e"),
                        null, UUID.fromString("0198c64b-086a-796d-a4d4-8d44881d2ad7"), "안연아바보",
                        null, "강남구 삼성동", 127.043999, 37.510297, null, drivingInfo, drivingRoutes, 0),
                new RouteResponse(false, false, UUID.fromString("0198c64d-780c-736b-9ba9-589632f5f136"),
                        null, UUID.fromString("0198c64d-780c-736b-9baa-a3f2fe33c325"), "나야나",
                        null, "동작구 상도동", 126.95781764313084, 37.4963172817574, null, drivingInfo, drivingRoutes, 0)
        );

        List<MeetingPointRouteGroup> groups = List.of(
                new MeetingPointRouteGroup(
                        240, 0,
                        new MeetingPoint("논현", 127.021385, 37.511108),
                        routeResponses,
                        new ParkingLotResponse("강남대로150길(구)", 127.0201132, 37.5156578, 517.33672526)
                ),
                new MeetingPointRouteGroup(
                        80, 0,
                        new MeetingPoint("신사", 127.020247, 37.516438),
                        routeResponses,
                        new ParkingLotResponse("강남대로150길(구)", 127.020586, 37.515837, 73.1268062)
                ),
                new MeetingPointRouteGroup(
                        32, 0,
                        new MeetingPoint("교대", 127.014631, 37.493957),
                        routeResponses,
                        new ParkingLotResponse("파미에(반포천) 주차장(시)", 127.007933, 37.504517, 1313.17667989)
                )
        );

        return new MeetingPointRouteGroups(groups);
    }
}
