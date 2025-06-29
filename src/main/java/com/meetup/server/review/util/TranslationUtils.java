package com.meetup.server.review.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class TranslationUtils {

    private static final Pattern KOREAN_PATTERN = Pattern.compile(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");

    public static boolean isKorean(String text) {
        if (!StringUtils.hasText(text)) return true;
        return KOREAN_PATTERN.matcher(text).matches();
    }
}
