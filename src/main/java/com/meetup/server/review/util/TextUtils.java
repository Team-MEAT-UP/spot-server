package com.meetup.server.review.util;

import java.util.regex.Pattern;

public class TextUtils {

    private static final Pattern KOREAN_PATTERN = Pattern.compile(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");

    public static boolean isKorean(String text) {
        if (text == null) return false;
        return KOREAN_PATTERN.matcher(text).matches();
    }
}
