package com.meetup.server.admin.application;

import com.meetup.server.admin.dto.request.AdminRegisterRequest;
import com.meetup.server.admin.dto.response.AdminEventResponse;
import com.meetup.server.admin.dto.response.AdminUserResponse;
import com.meetup.server.admin.dto.response.DailyEventStatsResponse;
import com.meetup.server.admin.dto.response.DailyUserStatsResponse;
import com.meetup.server.admin.implement.AdminValidator;
import com.meetup.server.admin.implement.AdminWriter;
import com.meetup.server.event.domain.Event;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.startpoint.implement.StartPointReader;
import com.meetup.server.startpoint.infrastructure.querydsl.projection.ParticipantCount;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.implement.LogUserLoginReader;
import com.meetup.server.user.implement.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final LogUserLoginReader logUserLoginReader;
    private final UserReader userReader;

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

    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userReader.readAll(pageable);

        List<AdminUserResponse> adminUserResponses = users.getContent().stream()
                .map(AdminUserResponse::from)
                .toList();

        return new PageImpl<>(
                adminUserResponses,
                pageable,
                users.getTotalElements()
        );
    }

    public DailyEventStatsResponse getDailyEventStats() {
        LocalDate todayDate = LocalDate.now();
        long dailyEventCount = eventReader.readDailyEventCount(todayDate);
        long dailyParticipantCount = startPointReader.readDailyParticipantCount(todayDate);

        return DailyEventStatsResponse.of(dailyEventCount, dailyParticipantCount);
    }

    public DailyUserStatsResponse getDailyUserStats() {
        LocalDate todayDate = LocalDate.now();
        long dailyLoginUserCount = logUserLoginReader.readDailyLoginUserCount(todayDate);
        long dailyRegisterUserCount = userReader.readDailyRegisterUserCount(todayDate);

        return DailyUserStatsResponse.of(dailyLoginUserCount, dailyRegisterUserCount);
    }
}
