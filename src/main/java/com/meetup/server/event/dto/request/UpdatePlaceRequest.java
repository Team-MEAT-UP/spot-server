package com.meetup.server.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdatePlaceRequest(

        @NotNull
        @Schema(description = "확정 장소 ID", example = "0196f346-5244-79b9-85b0-955d6328f09b")
        UUID placeId,

        @Schema(description = "지하철역 ID", example = "1")
        int subwayId
) {
}
