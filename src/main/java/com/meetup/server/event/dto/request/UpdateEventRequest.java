package com.meetup.server.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record UpdateEventRequest(

        @NotBlank
        @Size(min = 1, max = 50)
        @Schema(description = "모임명", example = "입력핑")
        String eventName,

        @NotNull
        @Schema(description = "모임 날짜", example = "2026-01-01")
        LocalDate eventDate,

        @NotNull
        @Schema(description = "모임 시간", example = "15:30")
        LocalTime eventTime
) {
    public LocalDateTime toDateTime() {
        return LocalDateTime.of(eventDate, eventTime);
    }
}
