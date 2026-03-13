package com.meetup.server.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ProfileUtil {

    private final Environment environment;

    public boolean isProd() {
        return isProfileActive("prod");
    }

    public boolean isStg() {
        return isProfileActive("stg");
    }

    public boolean isLocal() {
        return isProfileActive("local");
    }

    public String getActiveProfile() {
        String[] profiles = environment.getActiveProfiles();
        return (profiles.length > 0) ? profiles[0].toUpperCase() : "DEFAULT";
    }

    private boolean isProfileActive(String profile) {
        return Arrays.asList(environment.getActiveProfiles()).contains(profile);
    }
}
