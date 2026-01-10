package com.meetup.server.log.presentation;

import com.meetup.server.global.support.response.ApiResponse;
import com.meetup.server.log.application.LogService;
import com.meetup.server.log.dto.request.LogEventInflowRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping("/inflow")
    public ApiResponse<?> logEventInflow(
            @Valid @RequestBody LogEventInflowRequest logEventInflowRequest,
            @AuthenticationPrincipal Long userId,
            HttpServletRequest request
    ) {
        String ipAddress = request.getHeader("X-Real-IP");
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        logService.logEventInflow(logEventInflowRequest, userId, ipAddress, userAgent);
        return ApiResponse.success();
    }
}
