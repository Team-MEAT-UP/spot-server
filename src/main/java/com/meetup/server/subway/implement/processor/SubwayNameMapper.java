package com.meetup.server.subway.implement.processor;

import com.meetup.server.subway.domain.Subway;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubwayNameMapper {

    private static final Pattern PARENTHESES_PATTERN = Pattern.compile("\\(.*?\\)");
    private static final Pattern STATION_SUFFIX_PATTERN = Pattern.compile("역$");

    public static String normalizeStationName(String name) {
        if (name == null) {
            return "";
        }

        String withoutParentheses = PARENTHESES_PATTERN.matcher(name).replaceAll("").trim();
        return STATION_SUFFIX_PATTERN.matcher(withoutParentheses).replaceAll("");
    }

    public static String toApiStationName(Subway subway) {
        if (subway == null) {
            return "";
        }
        return normalizeStationName(subway.getName());
    }
}
