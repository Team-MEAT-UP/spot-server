package com.meetup.server.startpoint.implement;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.implement.EventValidator;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.domain.type.Address;
import com.meetup.server.startpoint.domain.type.Location;
import com.meetup.server.startpoint.dto.request.StartPointRequest;
import com.meetup.server.startpoint.persistence.StartPointRepository;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.implement.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StartPointProcessor {

    private final StartPointRepository startPointRepository;
    private final EventValidator eventValidator;
    private final UserReader userReader;

    public StartPoint save(Event event, Long userId, UUID guestId, StartPointRequest startPointRequest) {
        Optional<User> optionalUser = userReader.readUserIfExists(userId);
        if (optionalUser.isPresent()) {
            return saveByUser(event, optionalUser.get(), startPointRequest);
        }

        return saveByGuest(event, guestId, startPointRequest);
    }

    public StartPoint saveByUser(Event event, User user, StartPointRequest startPointRequest) {
        eventValidator.validateEventIsNotFull(event);
        StartPoint startPoint = StartPoint.builder()
                .event(event)
                .user(user)
                .name(startPointRequest.startPoint())
                .address(Address.of(startPointRequest.address(), startPointRequest.roadAddress()))
                .location(Location.of(startPointRequest.longitude(), startPointRequest.latitude()))
                .point(CoordinateUtil.createPoint(startPointRequest.longitude(), startPointRequest.latitude()))
                .isUser(true)
                .isTransit(startPointRequest.isTransit())
                .build();

        return startPointRepository.save(startPoint);
    }

    public StartPoint saveByGuest(Event event, UUID guestId, StartPointRequest startPointRequest) {
        eventValidator.validateEventIsNotFull(event);
        StartPoint startPoint = StartPoint.builder()
                .event(event)
                .user(null)
                .name(startPointRequest.startPoint())
                .address(Address.of(startPointRequest.address(), startPointRequest.roadAddress()))
                .location(Location.of(startPointRequest.longitude(), startPointRequest.latitude()))
                .point(CoordinateUtil.createPoint(startPointRequest.longitude(), startPointRequest.latitude()))
                .isUser(false)
                .nonUserName(startPointRequest.username())
                .guestId(guestId)
                .isTransit(startPointRequest.isTransit())
                .build();

        return startPointRepository.save(startPoint);
    }

    public void update(StartPoint startPoint, StartPointRequest startPointRequest) {
        startPoint.updateStartPoint(
                startPointRequest.startPoint(),
                Address.of(startPointRequest.address(), startPointRequest.roadAddress()),
                Location.of(startPointRequest.longitude(), startPointRequest.latitude()),
                startPointRequest.username(),
                CoordinateUtil.createPoint(startPointRequest.longitude(), startPointRequest.latitude()),
                startPointRequest.isTransit()
        );
    }

    public void delete(StartPoint startPoint) {
        startPointRepository.delete(startPoint);
    }
}
