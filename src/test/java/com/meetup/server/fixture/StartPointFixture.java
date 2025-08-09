package com.meetup.server.fixture;

import com.meetup.server.event.domain.Event;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.domain.type.Address;
import com.meetup.server.startpoint.domain.type.Location;
import com.meetup.server.startpoint.dto.request.StartPointRequest;
import com.meetup.server.user.domain.User;

public class StartPointFixture {

    public static StartPointRequest getStartPointRequest() {
        return new StartPointRequest(
                "땡수팟", "선정릉역 수인분당선", "서울특별시 강남구 삼성동 111-114", "서울특별시 강남구 선릉로 지하580",
                127.043999, 37.510297
        );
    }

    public static StartPoint getStartPoint(Event event, User user) {
        return StartPoint.builder()
                .event(event)
                .user(user)
                .name("선정릉역 수인분당선")
                .address(Address.of("서울특별시 강남구 삼성동 111-114", "서울특별시 강남구 선릉로 지하580"))
                .location(Location.of(127.043999, 37.510297))
                .point(CoordinateUtil.createPoint(127.043999, 37.510297))
                .build();
    }
}
