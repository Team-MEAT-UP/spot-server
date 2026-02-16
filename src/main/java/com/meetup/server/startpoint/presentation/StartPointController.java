package com.meetup.server.startpoint.presentation;

import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.global.clients.kakao.local.KakaoLocalResponse;
import com.meetup.server.global.support.response.ApiResponse;
import com.meetup.server.startpoint.application.StartPointService;
import com.meetup.server.startpoint.dto.request.StartPointRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StartPointController {

    private final StartPointService startPointService;

    @GetMapping("/start-points/search")
    public ApiResponse<KakaoLocalResponse> searchStartPoint(
            @RequestParam(name = "textQuery") String textQuery
    ) {
        KakaoLocalResponse response = startPointService.searchStartPoint(textQuery);
        return ApiResponse.success(response);
    }

    @PostMapping("/events/{eventId}/start-points")
    public ApiResponse<EventStartPointResponse> createStartPoint(
            @PathVariable UUID eventId,
            @Valid @RequestBody StartPointRequest startPointRequest,
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) UUID guestId
    ) {
        return ApiResponse.success(startPointService.createStartPoint(eventId, userId, guestId, startPointRequest));
    }

    @PatchMapping("/events/{eventId}/start-points/{startPointId}")
    public ApiResponse<EventStartPointResponse> updateStartPoint(
            @PathVariable UUID eventId,
            @PathVariable UUID startPointId,
            @Valid @RequestBody StartPointRequest startPointRequest
    ) {
        return ApiResponse.success(startPointService.updateStartPoint(eventId, startPointId, startPointRequest));
    }

    @DeleteMapping("/events/{eventId}/start-points/{startPointId}")
    public ApiResponse<?> deleteStartPoint(
            @PathVariable UUID eventId,
            @PathVariable UUID startPointId
    ) {
        startPointService.deleteStartPoint(eventId, startPointId);
        return ApiResponse.success();
    }
}
