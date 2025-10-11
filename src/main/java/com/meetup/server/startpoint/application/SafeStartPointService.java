package com.meetup.server.startpoint.application;

import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.startpoint.dto.request.StartPointRequest;
import com.meetup.server.startpoint.exception.StartPointErrorType;
import com.meetup.server.startpoint.exception.StartPointException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SafeStartPointService {

    private final StartPointService startPointService;

    public EventStartPointResponse createStartPointSafe(UUID eventId, Long userId, UUID guestId, StartPointRequest request) {
        try {
            return startPointService.createStartPoint(eventId, userId, guestId, request);
        } catch (CannotAcquireLockException | OptimisticLockingFailureException e) {
            throw new StartPointException(StartPointErrorType.START_POINT_CONFLICT);
        }
    }
}
