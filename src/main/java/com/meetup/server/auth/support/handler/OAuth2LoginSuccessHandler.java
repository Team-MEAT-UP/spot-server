package com.meetup.server.auth.support.handler;

import com.meetup.server.auth.dto.CustomOAuth2User;
import com.meetup.server.auth.support.CookieUtil;
import com.meetup.server.global.support.jwt.JwtTokenProvider;
import com.meetup.server.global.util.ProfileUtil;
import com.meetup.server.log.domain.type.LoginStatus;
import com.meetup.server.log.implement.LogUserLoginWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final CookieUtil cookieUtil;
    private final LogUserLoginWriter logUserLoginWriter;
    private final ProfileUtil profileUtil;

    @Value("${app.oauth2.successRedirectUri}")
    private String successRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String ipAddress = request.getHeader("X-Real-IP");
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        logUserLoginWriter.save(oAuth2User.getUserId(), LoginStatus.SUCCESS, ipAddress, userAgent, null);

        String accessToken = tokenProvider.createAccessToken(oAuth2User);
        String refreshToken = tokenProvider.createRefreshToken(oAuth2User);

        cookieUtil.setAccessTokenCookie(response, accessToken);
        cookieUtil.setRefreshTokenCookie(response, refreshToken);

        String targetUrl = createRedirectUrlWithTokens(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String createRedirectUrlWithTokens(HttpServletRequest request) {
        StateParams stateParams = parseState(request.getParameter("state"));
        String baseUrl = resolveBaseUrl(stateParams);

        String redirectUrl;
        if ("visited".equals(stateParams.to) && stateParams.eventId != null && stateParams.placeId != null) {
            redirectUrl = buildVisitedRedirectUrl(baseUrl, stateParams.eventId, stateParams.placeId);
        } else if ("notvisited".equals(stateParams.to) && stateParams.eventId != null && stateParams.placeId != null) {
            redirectUrl = buildNotVisitedRedirectUrl(baseUrl, stateParams.eventId, stateParams.placeId);
        } else {
            redirectUrl = buildDefaultCallbackUrl(baseUrl, stateParams.eventId, stateParams.to);
        }

        return redirectUrl;
    }

    private String resolveBaseUrl(StateParams stateParams) {
        if ("local".equals(stateParams.env) && profileUtil.isStg()) {
            return "http://localhost:5173";
        }
        return successRedirectUri;
    }

    private StateParams parseState(String state) {
        StateParams params = new StateParams();

        if (state == null || state.isBlank()) return params;

        String decodedState = java.net.URLDecoder.decode(state, java.nio.charset.StandardCharsets.UTF_8);

        for (String param : decodedState.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                switch (keyValue[0]) {
                    case "to" -> params.to = keyValue[1];
                    case "eventId" -> params.eventId = keyValue[1];
                    case "placeId" -> params.placeId = keyValue[1];
                    case "env" -> params.env = keyValue[1];
                }
            }
        }
        return params;
    }

    private String buildDefaultCallbackUrl(String baseUrl, String eventId, String to) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl).pathSegment("oauth", "kakao", "callback");
        if (eventId != null && !eventId.isBlank()) {
            uriBuilder.queryParam("eventId", eventId);
        }
        if (to != null && !to.isBlank()) {
            uriBuilder.queryParam("to", to);
        }
        return uriBuilder.build().toUriString();
    }

    private String buildVisitedRedirectUrl(String baseUrl, String eventId, String placeId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("visited", eventId, placeId)
                .build()
                .toUriString();
    }

    private String buildNotVisitedRedirectUrl(String baseUrl, String eventId, String placeId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("notvisited", eventId, placeId)
                .build()
                .toUriString();
    }

    private static class StateParams {
        String to;
        String eventId;
        String placeId;
        String env;
    }
}
