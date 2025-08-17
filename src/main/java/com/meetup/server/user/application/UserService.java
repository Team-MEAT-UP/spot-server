package com.meetup.server.user.application;

import com.meetup.server.startpoint.implement.StartPointProcessor;
import com.meetup.server.startpoint.implement.StartPointReader;
import com.meetup.server.startpoint.persistence.projection.EventHistory;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.dto.response.UserEventHistoryResponse;
import com.meetup.server.user.dto.response.UserEventHistoryResponseList;
import com.meetup.server.user.dto.response.UserProfileInfoResponse;
import com.meetup.server.user.implement.AgreementValidator;
import com.meetup.server.user.implement.UserEventHistoryAssembler;
import com.meetup.server.user.implement.UserReader;
import com.meetup.server.user.implement.UserWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final StartPointReader startPointReader;
    private final AgreementValidator agreementValidator;
    private final UserReader userReader;
    private final UserEventHistoryAssembler userEventHistoryAssembler;
    private final UserWriter userWriter;

    public UserProfileInfoResponse getUserProfileInfo(Long userId) {
        return UserProfileInfoResponse.from(userReader.read(userId));
    }

    @Transactional
    public void saveUserAgreement(Long userId, boolean personalInfoAgreement, boolean marketingAgreement) {
        agreementValidator.validateAgreements(personalInfoAgreement);

        User user = userReader.read(userId);
        user.updateAgreement(personalInfoAgreement, marketingAgreement);
    }

    public UserEventHistoryResponseList getUserEventHistory(Long userId, UUID lastViewedEventId, int size) {
        List<EventHistory> fetchedEvents = startPointReader.readEventHistories(userId, lastViewedEventId, size + 1);
        List<UserEventHistoryResponse> responses = userEventHistoryAssembler.assemble(fetchedEvents, userId);

        boolean hasNext = responses.size() > size;
        List<UserEventHistoryResponse> displayResponses = hasNext ? responses.subList(0, size) : responses;
        UUID lastEventId = displayResponses.isEmpty() ? null : displayResponses.getLast().eventId();

        return UserEventHistoryResponseList.of(displayResponses, hasNext, lastEventId);

    }

    @Transactional
    public void updateNickname(Long userId, String nickname) {
        User user = userReader.read(userId);
        user.updateNickname(nickname);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userReader.read(userId);
        userWriter.withdraw(user);
    }
}
