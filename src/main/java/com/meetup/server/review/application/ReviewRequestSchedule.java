package com.meetup.server.review.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.event.util.UsernameExtractor;
import com.meetup.server.global.email.EmailSender;
import com.meetup.server.global.email.domain.EmailSendHistory;
import com.meetup.server.global.email.domain.EmailSendStatus;
import com.meetup.server.global.email.domain.EmailType;
import com.meetup.server.global.email.dto.EmailSendRequest;
import com.meetup.server.global.email.infrastructure.jpa.EmailSendHistoryRepository;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.value.OpeningHour;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.implement.StartPointReader;
import com.meetup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.meetup.server.global.util.TimeUtil.KST_ZONE_ID;

@Service
@RequiredArgsConstructor
public class ReviewRequestSchedule {

    @Value("${server-url}")
    private String serverUrl;

    private static final String EVERY_30_MINUTES = "0 0,30 * * * *";
    private static final int REVIEW_REQUEST_HOURS_AHEAD = 4;
    private static final String TEMPLATE_REVIEW_REQUEST = "review_request_email";
    private static final String SUBJECT_FORMAT = "[모이삼] 오늘 ‘%s’은 어떠셨나요?";

    private final EmailSender emailSender;
    private final EventReader eventReader;
    private final StartPointReader startPointReader;
    private final EmailSendHistoryRepository emailSendHistoryRepository;
    private final TemplateEngine templateEngine;

    @Scheduled(cron = EVERY_30_MINUTES, zone = "Asia/Seoul")
    public void sendReviewRequestEmail() {
        List<Event> events = eventReader.readEventsWithPlaceAtHour(REVIEW_REQUEST_HOURS_AHEAD);
        if (events.isEmpty()) return;

        List<EmailSendHistory> eventSendHistories = emailSendHistoryRepository.findAllByEventInAndEmailType(events, EmailType.REVIEW_REQUEST);

        Map<String, EmailSendHistory> emailHistoryByEventAndUser = eventSendHistories.stream()
                .collect(Collectors.toMap(
                        history -> history.getEvent().getEventId() + "_" + history.getUser().getUserId(),
                        Function.identity()
                ));

        List<EmailSendHistory> historiesToSave = new ArrayList<>();

        for (Event event : events) {
            if (event.getPlace() == null) continue;

            List<StartPoint> participants = startPointReader.readAllWithUserByEvent(event);

            for (StartPoint participant : participants) {
                User user = participant.getUser();
                if (user == null) continue;

                String key = event.getEventId() + "_" + user.getUserId();
                if (emailHistoryByEventAndUser.containsKey(key)) continue;

                Context context = buildContext(event, participant, participants);
                String html = templateEngine.process(TEMPLATE_REVIEW_REQUEST, context);

                EmailSendRequest request = new EmailSendRequest(
                        user.getEmail(),
                        String.format(SUBJECT_FORMAT, event.getEventName()),
                        html
                );

                EmailSendHistory.EmailSendHistoryBuilder emailSendHistoryBuilder = EmailSendHistory.builder()
                        .event(event)
                        .user(user)
                        .emailType(EmailType.REVIEW_REQUEST)
                        .sentAt(LocalDateTime.now());

                try {
                    emailSender.send(request);
                    emailSendHistoryBuilder.emailSendStatus(EmailSendStatus.SUCCESS);
                } catch (Exception e) {
                    emailSendHistoryBuilder.emailSendStatus(EmailSendStatus.FAILURE);
                    emailSendHistoryBuilder.errorMessage(e.getMessage());
                }

                historiesToSave.add(emailSendHistoryBuilder.build());
            }
        }

        emailSendHistoryRepository.saveAll(historiesToSave);
    }

    private Context buildContext(Event event, StartPoint participant, List<StartPoint> participants) {
        Place place = event.getPlace();
        User currentUser = participant.getUser();

        List<String> otherParticipants = participants.stream()
                .filter(startPoint -> !participant.getStartPointId().equals(startPoint.getStartPointId()))
                .map(UsernameExtractor::extractDisplayName)
                .toList();

        Context context = new Context();
        context.setVariable("eventName", event.getEventName());
        context.setVariable("participantName", currentUser.getNickname());
        context.setVariable("otherParticipantName", otherParticipants.getFirst());
        context.setVariable("otherParticipantCount", otherParticipants.size());
        context.setVariable("placeName", place.getName());
        context.setVariable("placeRating", place.getGoogleRating());
        context.setVariable("placeImage", Optional.ofNullable(place.getImages())
                .filter(images -> !images.isEmpty())
                .map(images -> images.getFirst().photoUri())
                .orElse(null));
        context.setVariable("serverUrl", serverUrl);
        context.setVariable("eventId", event.getEventId());
        context.setVariable("placeId", place.getId());

        Optional<OpeningHour> todayOpeningHour = Optional.ofNullable(place.getOpeningHours())
                .orElse(List.of())
                .stream()
                .filter(openingHour -> openingHour.isToday(LocalDateTime.now(KST_ZONE_ID)))
                .findFirst();

        context.setVariable("openTime", todayOpeningHour.map(OpeningHour::openTime).orElse(null));
        context.setVariable("closeTime", todayOpeningHour.map(OpeningHour::closeTime).orElse(null));
        return context;
    }
}
