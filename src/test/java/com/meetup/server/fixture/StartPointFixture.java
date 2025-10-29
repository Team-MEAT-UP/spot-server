package com.meetup.server.fixture;

import com.meetup.server.event.domain.Event;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.domain.type.Address;
import com.meetup.server.startpoint.domain.type.Location;
import com.meetup.server.startpoint.dto.request.StartPointRequest;
import com.meetup.server.user.domain.User;

import java.util.UUID;

public class StartPointFixture {

    public static final UUID START_POINT_ID = UUID.fromString("01968fe2-5277-712a-ad3c-98f29c2782e0");
    public static final UUID GUEST_ID = UUID.fromString("01968fe2-5277-712a-ad3c-98f29c2782e1");

    public static StartPointRequest getStartPointRequest() {
        return new StartPointRequest(
                "땡수팟", "선정릉역 수인분당선", "서울특별시 강남구 삼성동 111-114", "서울특별시 강남구 선릉로 지하580",
                127.043999, 37.510297, true
        );
    }

    public static StartPoint getStartPoint(Event event, User user) {
        return StartPoint.builder()
                .startPointId(START_POINT_ID)
                .event(event)
                .user(user)
                .name("선정릉역 수인분당선")
                .address(Address.of("서울특별시 강남구 삼성동 111-114", "서울특별시 강남구 선릉로 지하580"))
                .location(Location.of(127.043999, 37.510297))
                .point(CoordinateUtil.createPoint(127.043999, 37.510297))
                .isUser(user != null)
                .guestId(GUEST_ID)
                .nonUserName("땡수팟")
                .isTransit(true)
                .build();
    }
}
