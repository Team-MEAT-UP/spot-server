package com.meetup.server.log.dto.request;

import com.meetup.server.log.domain.type.InflowType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LogEventInflowRequest(
        @NotNull(message = "유입 경로는 필수 값입니다.")
        InflowType inflowType,

        @NotNull(message = "이벤트 ID는 필수 값입니다.")
        UUID eventId
) {
}
