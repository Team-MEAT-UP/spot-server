package com.meetup.server.event.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdatePlaceRequest(
        @NotNull
        UUID placeId,

        int subwayId
) {
}
