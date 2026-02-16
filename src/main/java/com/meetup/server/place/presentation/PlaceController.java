package com.meetup.server.place.presentation;

import com.meetup.server.global.support.response.ApiResponse;
import com.meetup.server.place.application.PlaceService;
import com.meetup.server.place.dto.response.PlaceDetailResponse;
import com.meetup.server.place.dto.response.PlaceResponseList;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping
    public ApiResponse<PlaceResponseList> getAllPlaces(
            @RequestParam UUID eventId,
            @RequestParam int subwayId
    ) {
        return ApiResponse.success(placeService.getAllPlaces(eventId, subwayId));
    }

    @GetMapping("/{placeId}")
    public ApiResponse<PlaceDetailResponse> getPlaceDetails(
            @PathVariable UUID placeId,
            @RequestParam UUID eventId,
            @RequestParam int subwayId
    ) {
        return ApiResponse.success(placeService.getPlace(eventId, placeId, subwayId));
    }

    @GetMapping("/review")
    public ApiResponse<PlaceResponseList> getPlaceForReview(@RequestParam UUID eventId) {
        return ApiResponse.success(placeService.getPlaceForReview(eventId));
    }
}
