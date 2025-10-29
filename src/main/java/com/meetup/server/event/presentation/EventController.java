package com.meetup.server.event.presentation;

import com.meetup.server.event.application.EventService;
import com.meetup.server.event.dto.request.EventRequest;
import com.meetup.server.event.dto.request.UpdateEventRequest;
import com.meetup.server.event.dto.request.UpdatePlaceRequest;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.event.dto.response.route.MeetingPointRoutesResponse;
import com.meetup.server.global.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ApiResponse<EventStartPointResponse> createEvent(
            @Valid @RequestBody EventRequest eventRequest,
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) UUID guestId) {
        return ApiResponse.success(eventService.createEvent(userId, guestId, eventRequest));
    }

    @PatchMapping("/{eventId}")
    public ApiResponse<?> updateEvent(@PathVariable UUID eventId, @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        eventService.updateEvent(eventId, updateEventRequest);
        return ApiResponse.success();
    }

    @DeleteMapping("/{eventId}")
    public ApiResponse<?> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId);
        return ApiResponse.success();
    }

    @GetMapping("/{eventId}")
    public ApiResponse<MeetingPointRoutesResponse> getMeetingPointRoutes(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) UUID guestId) {
        return ApiResponse.success(eventService.getMeetingPointRoutes(eventId, userId, guestId));
    }

    @PatchMapping("/{eventId}/place")
    public ApiResponse<?> updatePlace(
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdatePlaceRequest updatePlaceRequest
    ) {
        eventService.updatePlace(eventId, updatePlaceRequest);
        return ApiResponse.success();
    }

    @DeleteMapping("/{eventId}/place")
    public ApiResponse<?> deletePlace(@PathVariable UUID eventId) {
        eventService.deletePlace(eventId);
        return ApiResponse.success();
    }
}
