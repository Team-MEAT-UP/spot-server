package com.meetup.server.admin.application;

import com.meetup.server.admin.dto.request.AdminRegisterRequest;
import com.meetup.server.admin.dto.response.AdminEventResponse;
import com.meetup.server.admin.implement.AdminValidator;
import com.meetup.server.admin.implement.AdminWriter;
import com.meetup.server.event.domain.Event;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.startpoint.implement.StartPointReader;
import com.meetup.server.startpoint.infrastructure.querydsl.projection.ParticipantCount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminValidator adminValidator;
    private final AdminWriter adminWriter;
    private final EventReader eventReader;
    private final StartPointReader startPointReader;

    @Transactional
    public void register(AdminRegisterRequest request) {
        adminValidator.validateAdminNotAlreadyExists(request.username());
        adminWriter.save(request.name(), request.username(), request.password());
    }

    public Page<AdminEventResponse> getAllEvents(Pageable pageable) {
        Page<Event> events = eventReader.readAll(pageable);

        List<UUID> eventIds = events.stream()
                .map(Event::getEventId)
                .toList();

        List<ParticipantCount> participantCounts = startPointReader.readParticipantCounts(eventIds);

        Map<UUID, Integer> eventParticipantCountMap = participantCounts.stream()
                .collect(Collectors.toMap(
                        ParticipantCount::eventId,
                        participant -> participant.count().intValue()
                ));

        List<AdminEventResponse> adminEventResponses = events.getContent().stream()
                .map(event -> {
                    int count = eventParticipantCountMap.getOrDefault(event.getEventId(), 0);
                    return AdminEventResponse.of(event, count);
                })
                .toList();

        return new PageImpl<>(
                adminEventResponses,
                pageable,
                events.getTotalElements()
        );
    }
}
