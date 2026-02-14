package com.meetup.server.auth.support.handler;

import com.meetup.server.auth.exception.AuthErrorType;
import com.meetup.server.global.util.LoggingUtil;
import com.meetup.server.log.domain.type.LoginStatus;
import com.meetup.server.log.implement.LogUserLoginWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LogUserLoginWriter logUserLoginWriter;

    @Value("${app.oauth2.failureRedirectUri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception
    ) throws IOException {

        String ipAddress = request.getHeader("X-Real-IP");
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        logUserLoginWriter.save(null, LoginStatus.FAILURE, ipAddress, userAgent, exception.getMessage());

        LoggingUtil.logError("[OAuth2Failed]", AuthErrorType.FAILED_OAUTH_AUTHENTICATION, exception);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
