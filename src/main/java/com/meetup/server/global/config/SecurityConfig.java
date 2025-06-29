package com.meetup.server.global.config;

import com.meetup.server.auth.application.CustomOAuth2UserService;
import com.meetup.server.auth.presentation.filter.*;
import com.meetup.server.auth.support.handler.OAuth2LoginFailureHandler;
import com.meetup.server.auth.support.handler.OAuth2LoginSuccessHandler;
import com.meetup.server.auth.support.resolver.CustomAuthorizationRequestResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnBean(SwaggerAuthFilter.class)
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final ResponseContextFilter responseContextFilter;
    private final SwaggerAuthFilter swaggerAuthFilter;

    private final String[] BLACK_LIST = {
            "/historys/**",
            "/users/**",
            "/auth/**",
    };

    @Bean
    public CustomAuthorizationRequestResolver customAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        return new CustomAuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAuthorizationRequestResolver customAuthorizationRequestResolver) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(BLACK_LIST).authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling
                            .authenticationEntryPoint(jwtAuthenticationEntryPoint) //401
                            .accessDeniedHandler(jwtAccessDeniedHandler); //403
                })
                .oauth2Login(oauth ->
                        oauth
                                .authorizationEndpoint(endpoint ->
                                        endpoint.authorizationRequestResolver(customAuthorizationRequestResolver)
                                )
                                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                                .successHandler(oAuth2LoginSuccessHandler)
                                .failureHandler(oAuth2LoginFailureHandler)
                )
                .addFilterBefore(swaggerAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(responseContextFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
