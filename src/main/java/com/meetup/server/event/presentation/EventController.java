package com.meetup.server.event.presentation;

import com.meetup.server.event.application.EventCacheService;
import com.meetup.server.event.application.EventService;
import com.meetup.server.event.dto.request.EventRequest;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.event.dto.response.RouteResponseList;
import com.meetup.server.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Event API", description = "모임 API")
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventCacheService eventCacheService;

    @Operation(summary = "모임 생성 API", description = "모임 생성자의 출발지를 입력받아 모임을 생성합니다")
    @PostMapping
    public ApiResponse<EventStartPointResponse> createEvent(
            @Valid @RequestBody EventRequest eventRequest,
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) UUID guestId) {
        return ApiResponse.success(eventService.createEvent(userId, guestId, eventRequest));
    }

    @Operation(summary = "지도 조회 API", description = "모임의 중간 지점 계산 및 모임 참여자의 경로 조회를 진행합니다")
    @GetMapping("/{eventId}")
    public ApiResponse<RouteResponseList> getEventMap(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) UUID guestId) {
        return ApiResponse.success(eventService.getEventMap(eventId, userId, guestId));
    }

    @Operation(summary = "대중교통 선택 API", description = "본인의 대중교통, 자가용 선택 여부를 설정합니다")
    @PatchMapping("/{eventId}")
    public ApiResponse<?> updateTransit(@PathVariable UUID eventId, @RequestParam UUID startPointId, @RequestParam boolean isTransit) {
        eventCacheService.updateTransit(eventId, startPointId, isTransit);
        return ApiResponse.success();
    }
}
