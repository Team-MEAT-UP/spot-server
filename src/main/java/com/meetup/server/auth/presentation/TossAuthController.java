package com.meetup.server.auth.presentation;

import com.meetup.server.auth.application.TossAuthService;
import com.meetup.server.auth.dto.request.TossLoginRequest;
import com.meetup.server.auth.dto.response.TossLoginResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/toss")
public class TossAuthController {

    private final TossAuthService tossAuthService;

    @PostMapping("/login")
    public ResponseEntity<TossLoginResponse> login(
            @RequestBody TossLoginRequest request,
            HttpServletResponse response
    ) {
        TossLoginResponse loginResponse = tossAuthService.login(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    //테스트용도
    @PostMapping(
            value = "/login/redirect",
            consumes = "application/x-www-form-urlencoded"
    )
    public void loginRedirect(
            @ModelAttribute TossLoginRequest request,
            HttpServletResponse response
    ) throws IOException {
        tossAuthService.login(request, response);
        response.sendRedirect("https://staging.moisam.kr");
    }
}
