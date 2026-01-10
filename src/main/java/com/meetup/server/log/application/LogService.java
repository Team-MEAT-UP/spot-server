package com.meetup.server.log.application;

import com.meetup.server.log.dto.request.LogEventInflowRequest;
import com.meetup.server.log.implement.LogEventInflowWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogEventInflowWriter logEventInflowWriter;

    public void logEventInflow(LogEventInflowRequest logEventInflowRequest, Long userId, String ipAddress, String userAgent) {
        logEventInflowWriter.save(
                logEventInflowRequest.inflowType(),
                logEventInflowRequest.eventId(),
                userId,
                ipAddress,
                userAgent
        );
    }
}
