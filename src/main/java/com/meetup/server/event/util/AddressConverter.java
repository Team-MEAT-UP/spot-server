package com.meetup.server.event.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressConverter {

    private static final Pattern FULL_ADDRESS_PATTERN = Pattern.compile(
            "(?:(\\S+(도|특별시|광역시))\\s+)?" +
                    "(\\S+(시|군|구))" +
                    "(\\s+\\S+(구|군))?" +
                    "\\s+(\\S+(동|읍|면|가|로))"
    );

    private static final Pattern REGION_ONLY_PATTERN = Pattern.compile(
            "(?:(\\S+(도|특별시|광역시))\\s+)?" +
                    "(\\S+(시|군|구))"
    );

    public static String convertStartPointName(String address) {
        if (address == null || address.isBlank()) return "";

        Matcher fullMatcher = FULL_ADDRESS_PATTERN.matcher(address);

        if (fullMatcher.find()) {
            StringBuilder sb = new StringBuilder();

            String siGunGu = fullMatcher.group(3).trim();
            String gu = fullMatcher.group(5) != null ? fullMatcher.group(5).trim() : null;
            String dongEupMyeonGaRo = fullMatcher.group(7).trim();

            if (gu != null && !gu.isBlank()) {
                return sb.append(gu).append(" ")
                        .append(dongEupMyeonGaRo)
                        .toString();
            }

            return sb.append(siGunGu).append(" ")
                    .append(dongEupMyeonGaRo)
                    .toString();
        }

        Matcher regionMatcher = REGION_ONLY_PATTERN.matcher(address);

        if (regionMatcher.find()) {
            return regionMatcher.group(3).trim();
        }

        return "";
    }
}
