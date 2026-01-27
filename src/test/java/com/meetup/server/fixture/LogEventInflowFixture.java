package com.meetup.server.fixture;

import com.meetup.server.log.domain.type.InflowType;
import com.meetup.server.log.dto.request.LogEventInflowRequest;

public class LogEventInflowFixture {

    public static LogEventInflowRequest getLogEventInflowRequest() {
        return new LogEventInflowRequest(InflowType.KAKAO, EventFixture.EVENT_ID);
    }
}
