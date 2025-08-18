package com.meetup.server.user.presentation;

import com.meetup.server.global.support.response.ApiResponse;
import com.meetup.server.user.application.UserService;
import com.meetup.server.user.dto.request.UserAgreementRequest;
import com.meetup.server.user.dto.response.UserEventHistoryResponseList;
import com.meetup.server.user.dto.response.UserProfileInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User API", description = "사용자 API")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 프로필 정보 조회 API", description = "유저의 프로필 정보를 조회합니다")
    @GetMapping("")
    public ApiResponse<?> getUserProfileInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserProfileInfoResponse response = userService.getUserProfileInfo(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "사용자 약관 동의 저장 API", description = "사용자의 개인정보 이용 동의와 마케팅 이용 동의를 저장합니다")
    @PostMapping("/agreement")
    public ApiResponse<?> saveUserAgreement(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserAgreementRequest request
    ) {
        userService.saveUserAgreement(userId, request.isPersonalInfoAgreement(), request.isMarketingAgreement());
        return ApiResponse.success();
    }

    @Operation(summary = "모임 히스토리 리스트 조회 API", description = "로그인 한 사용자가 참여한 모임 리스트를 조회합니다")
    @GetMapping("/events")
    public ApiResponse<UserEventHistoryResponseList> getUserEventHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "lastViewedEventId", required = false) UUID lastViewedEventId,
            @RequestParam(value = "size", required = true) int size
    ) {
        UserEventHistoryResponseList response = userService.getUserEventHistory(userId, lastViewedEventId, size);
        return ApiResponse.success(response);
    }

    @Operation(summary = "사용자 닉네임 수정 API", description = "사용자의 닉네임을 수정합니다")
    @PatchMapping("")
    public ApiResponse<?> updateNickname(
            @AuthenticationPrincipal Long userId,
            @RequestParam @NotBlank @Size(min = 1, max = 5) String nickname
    ) {
        userService.updateNickname(userId, nickname);
        return ApiResponse.success();
    }

    @Operation(summary = "사용자 탈퇴 API", description = "사용자를 탈퇴시킵니다")
    @DeleteMapping("")
    public ApiResponse<?> withdrawUser(
            @AuthenticationPrincipal Long userId
    ) {
        userService.withdraw(userId);
        return ApiResponse.success();
    }
}
