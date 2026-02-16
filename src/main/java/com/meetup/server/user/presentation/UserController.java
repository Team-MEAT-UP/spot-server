package com.meetup.server.user.presentation;

import com.meetup.server.global.support.response.ApiResponse;
import com.meetup.server.user.application.UserService;
import com.meetup.server.user.dto.request.UserAgreementRequest;
import com.meetup.server.user.dto.response.UserEventHistoryResponseList;
import com.meetup.server.user.dto.response.UserProfileInfoResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<?> getUserProfileInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserProfileInfoResponse response = userService.getUserProfileInfo(userId);
        return ApiResponse.success(response);
    }

    @PostMapping("/agreement")
    public ApiResponse<?> saveUserAgreement(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserAgreementRequest request
    ) {
        userService.saveUserAgreement(userId, request.isPersonalInfoAgreement(), request.isMarketingAgreement());
        return ApiResponse.success();
    }

    @GetMapping("/events")
    public ApiResponse<UserEventHistoryResponseList> getUserEventHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "lastViewedEventId", required = false) UUID lastViewedEventId,
            @RequestParam(value = "size", required = true) int size
    ) {
        UserEventHistoryResponseList response = userService.getUserEventHistory(userId, lastViewedEventId, size);
        return ApiResponse.success(response);
    }

    @PatchMapping
    public ApiResponse<?> updateNickname(
            @AuthenticationPrincipal Long userId,
            @RequestParam @NotBlank @Size(min = 1, max = 5) String nickname
    ) {
        userService.updateNickname(userId, nickname);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<?> withdrawUser(
            @AuthenticationPrincipal Long userId
    ) {
        userService.withdraw(userId);
        return ApiResponse.success();
    }
}
