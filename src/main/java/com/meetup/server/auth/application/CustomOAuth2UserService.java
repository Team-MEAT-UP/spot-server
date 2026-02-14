package com.meetup.server.auth.application;

import com.meetup.server.auth.dto.CustomOAuth2User;
import com.meetup.server.auth.dto.response.OAuthAttributes;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.infrastructure.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private static final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(); // 소셜 로그인 식별값
        Map<String, Object> attributes = oAuth2User.getAttributes(); // 소셜 로그인 userInfo

        OAuthAttributes extractAttributes = OAuthAttributes.of(userNameAttributeName, attributes);
        User createdUser = getOrGenerateUser(extractAttributes);

        // Default 객체 아닌, Custom 객체 생성 하여 반환
        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(createdUser.getRole().getAuthority())),
                attributes,
                extractAttributes.nameAttributeKey(),
                createdUser.getEmail(),
                createdUser.getUserId()
        );
    }

    private User getOrGenerateUser(OAuthAttributes attributes) {
        String socialId = attributes.oauth2UserInfo().getSocialId();

        return userRepository.findBySocialId(socialId)
                .map(user -> {
                    if (user.isDeleted()) {
                        log.info("[Rejoin] 탈퇴 후 재가입 - SocialId: {}, UserId: {}", socialId, user.getUserId());
                        user.rejoin(attributes.toEntity(attributes.oauth2UserInfo()));
                        return userRepository.save(user);
                    }
                    log.info("[Login] 로그인 - SocialId: {}, UserId: {}", socialId, user.getUserId());
                    return user;
                })
                .orElseGet(() -> {
                    log.info("[Register] 회원가입 - SocialId: {}", socialId);
                    User newUser = attributes.toEntity(attributes.oauth2UserInfo());
                    return userRepository.save(newUser);
                });
    }
}
