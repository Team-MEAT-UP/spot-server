package com.meetup.server.auth.presentation;

import com.meetup.server.auth.application.TossAuthService;
import com.meetup.server.auth.dto.request.TossLoginRequest;
import com.meetup.server.auth.dto.response.TossLoginResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.ok(tossAuthService.login(request, response));
    }
}
