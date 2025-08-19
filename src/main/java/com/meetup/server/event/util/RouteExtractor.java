package com.meetup.server.event.util;

import com.meetup.server.event.dto.response.route.DrivingInfoResponse;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RouteExtractor {

    public static int extractValidTransitTotalTime(OdsayTransitRouteSearchResponse transitResponse) {
        if (transitResponse == null || transitResponse.data() == null) {
            return 0;
        }

        var paths = transitResponse.data().path();
        if (paths == null || paths.isEmpty() || paths.getFirst() == null || paths.getFirst().info() == null) {
            return 0;
        }

        return paths.getFirst().info().totalTime();
    }

    public static int extractValidDrivingTotalTime(DrivingInfoResponse drivingInfoResponse) {
        if (drivingInfoResponse == null) {
            return 0;
        }
        return drivingInfoResponse.duration();
    }
}
