package com.meetup.server.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record UpdateEventRequest(
        @NotBlank
        @Size(min = 1, max = 50)
        String eventName,

        @NotNull
        LocalDate eventDate,

        @NotNull
        LocalTime eventTime
) {
    public LocalDateTime toDateTime() {
        return LocalDateTime.of(eventDate, eventTime);
    }
}
