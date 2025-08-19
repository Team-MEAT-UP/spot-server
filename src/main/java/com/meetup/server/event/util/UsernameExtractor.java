package com.meetup.server.event.util;

import com.meetup.server.global.util.StringUtil;
import com.meetup.server.startpoint.domain.StartPoint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UsernameExtractor {

    public static String extractDisplayName(StartPoint startPoint) {
        if (startPoint == null) {
            return null;
        }
        if (startPoint.getIsUser()) {
            return StringUtil.truncate(startPoint.getUser().getNickname(), 5);
        }
        return startPoint.getNonUserName();
    }
}
