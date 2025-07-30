package com.meetup.server.batch.presentation;

import com.meetup.server.batch.place.job.PlaceImageUpdateJob;
import com.meetup.server.batch.place.job.PlaceSaveJob;
import com.meetup.server.global.support.error.GlobalErrorType;
import com.meetup.server.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
public class BatchJobController {

    @Value("${batch.secret-key}")
    private String batchSecretKey;

    private final PlaceSaveJob placeSaveJob;
    private final PlaceImageUpdateJob placeImageUpdateJob;

    @Hidden
    @PostMapping("/place")
    public ApiResponse<?> runPlaceSaveJob(@RequestParam int page, @RequestHeader String secretKey) {
        if (!batchSecretKey.equals(secretKey)) {
            return ApiResponse.error(GlobalErrorType.UNAUTHORIZED);
        }

        placeSaveJob.savePlaceJobScheduler(page);
        return ApiResponse.success();
    }

    @Hidden
    @PostMapping("/place/image")
    public ApiResponse<?> runPlaceImageUpdateJob(@RequestHeader String secretKey) {
        if (!batchSecretKey.equals(secretKey)) {
            return ApiResponse.error(GlobalErrorType.UNAUTHORIZED);
        }

        placeImageUpdateJob.updatePlaceImageJobScheduler();
        return ApiResponse.success();
    }
}
