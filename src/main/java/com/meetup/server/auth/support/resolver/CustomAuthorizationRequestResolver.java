package com.meetup.server.auth.support.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo, String authorizationRequestBaseUri) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(request, req);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(request, req);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(HttpServletRequest request, OAuth2AuthorizationRequest req) {
        if (req == null) {
            return null;
        }

        String to = request.getParameter("to");
        String eventId = request.getParameter("eventId");
        String placeId = request.getParameter("placeId");
        String env = request.getParameter("env");

        if (to == null && eventId == null && placeId == null && env == null) {
            return req;
        }

        StringBuilder stateBuilder = new StringBuilder();
        if (to != null) stateBuilder.append("to=").append(to).append("&");
        if (eventId != null) stateBuilder.append("eventId=").append(eventId).append("&");
        if (placeId != null) stateBuilder.append("placeId=").append(placeId).append("&");
        if (env != null) stateBuilder.append("env=").append(env);

        String stateValue = stateBuilder.toString().replaceAll("&$", "");
        stateValue = URLEncoder.encode(stateValue, StandardCharsets.UTF_8);

        return OAuth2AuthorizationRequest.from(req)
                .state(stateValue)
                .build();
    }
}
