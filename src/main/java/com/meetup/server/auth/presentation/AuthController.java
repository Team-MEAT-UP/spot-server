package com.meetup.server.auth.presentation;

import com.meetup.server.auth.application.AuthService;
import com.meetup.server.global.support.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/test/{userId}")
    public String getAccessToken(@PathVariable Long userId) {
        return authService.createAccessTokenForUser(userId);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletResponse response) {
        authService.logout(response);
        return ApiResponse.success();
    }
}
