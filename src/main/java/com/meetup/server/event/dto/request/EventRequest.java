package com.meetup.server.event.dto.request;

import com.meetup.server.startpoint.dto.request.StartPointRequest;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record EventRequest(
        @NotBlank
        @Size(min = 1, max = 50)
        String eventName,

        @NotNull
        LocalDate eventDate,

        @NotNull
        LocalTime eventTime,

        @NotBlank
        @Size(min = 1, max = 5)
        String username,

        @NotBlank(message = "출발지명은 필수 값입니다.")
        String startPoint,

        @NotBlank(message = "지번주소는 필수 값입니다.")
        String address,

        @NotNull
        String roadAddress,

        @DecimalMin(value = "-180.0", message = "경도는 -180.0보다 크거나 같아야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180.0보다 작거나 같아야 합니다.")
        double longitude,

        @DecimalMin(value = "-90.0", message = "위도는 -90.0보다 크거나 같아야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90.0보다 작거나 같아야 합니다.")
        double latitude,

        @NotNull(message = "대중교통/자가용 선택 여부는 필수 값입니다.")
        boolean isTransit
) {
    public LocalDateTime toDateTime() {
        return LocalDateTime.of(eventDate, eventTime);
    }

    public StartPointRequest toStartPointRequest() {
        return new StartPointRequest(
                username,
                startPoint,
                address,
                roadAddress,
                longitude,
                latitude,
                isTransit
        );
    }
}
