package com.meetup.server.global.config;

import com.meetup.server.auth.support.CookieProperties;
import com.meetup.server.global.clients.clova.ClovaProperties;
import com.meetup.server.global.clients.google.place.GooglePlaceProperties;
import com.meetup.server.global.clients.kakao.local.KakaoLocalProperties;
import com.meetup.server.global.clients.kakao.mobility.KakaoMobilityProperties;
import com.meetup.server.global.clients.odsay.OdsayProperties;
import com.meetup.server.global.support.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {JwtProperties.class, KakaoLocalProperties.class, OdsayProperties.class, KakaoMobilityProperties.class, GooglePlaceProperties.class, CookieProperties.class, ClovaProperties.class})
public class ConfigurationPropertiesConfig {
}
