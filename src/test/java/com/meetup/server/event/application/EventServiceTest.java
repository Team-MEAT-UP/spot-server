package com.meetup.server.event.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.request.EventRequest;
import com.meetup.server.event.dto.request.UpdateEventRequest;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.event.infrastructure.jpa.EventRepository;
import com.meetup.server.event.infrastructure.redis.CachedRouteRepository;
import com.meetup.server.fixture.EventFixture;
import com.meetup.server.fixture.UserFixture;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.infrastructure.jpa.StartPointRepository;
import com.meetup.server.support.IntegrationTestContainer;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.infrastructure.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventServiceTest extends IntegrationTestContainer {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private StartPointRepository startPointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CachedRouteRepository cachedRouteRepository;

    @Autowired
    private EventReader eventReader;

    @Autowired
    private CacheManager cacheManager;

    private Event event;
    private User user;
    private EventRequest eventRequest;
    private UUID guestId;
    private UpdateEventRequest updateEventRequest;
    private MeetingPointRouteGroups meetingPointRouteGroups;
    private Event eventWithRoute;

    @BeforeEach
    void setUp() {
        event = eventRepository.save(EventFixture.getEvent());
        user = userRepository.save(UserFixture.getUser());
        eventRequest = EventFixture.getEventRequest();
        guestId = UUID.randomUUID();
        updateEventRequest = EventFixture.getUpdateEventRequest();
        eventWithRoute = eventRepository.save(EventFixture.getEventWithRoute());
        meetingPointRouteGroups = EventFixture.getMeetingPointRouteGroups();
    }

    @Test
    void 비로그인_사용자가_이벤트를_생성한다() {
        EventStartPointResponse eventStartPointResponse = eventService.createEvent(null, guestId, eventRequest);

        Optional<Event> optionalEvent = eventRepository.findById(eventStartPointResponse.eventId());
        assertThat(optionalEvent).isPresent();

        Optional<StartPoint> optionalStartPoint = startPointRepository.findById(eventStartPointResponse.startPointId());
        assertThat(optionalStartPoint).isPresent();
        assertThat(eventStartPointResponse.username()).isEqualTo(eventRequest.username());
        assertThat(optionalStartPoint.get().getStartPointId()).isEqualTo(eventStartPointResponse.startPointId());
        assertThat(optionalStartPoint.get().getName()).isEqualTo(eventRequest.startPoint());
        assertThat(optionalStartPoint.get().getUser()).isNull();
        assertThat(optionalStartPoint.get().getAddress().getAddress()).isEqualTo(eventRequest.address());
        assertThat(optionalStartPoint.get().getAddress().getRoadAddress()).isEqualTo(eventRequest.roadAddress());
        assertThat(optionalStartPoint.get().getLocation().getRoadLongitude()).isEqualTo(eventRequest.longitude());
        assertThat(optionalStartPoint.get().getLocation().getRoadLatitude()).isEqualTo(eventRequest.latitude());
        assertThat(optionalStartPoint.get().getGuestId()).isEqualTo(guestId);
    }

    @Transactional
    @Test
    void 로그인_사용자가_이벤트를_생성한다() {
        EventStartPointResponse eventStartPointResponse = eventService.createEvent(user.getUserId(), null, eventRequest);

        Optional<Event> optionalEvent = eventRepository.findById(eventStartPointResponse.eventId());
        assertThat(optionalEvent).isPresent();

        Optional<StartPoint> optionalStartPoint = startPointRepository.findById(eventStartPointResponse.startPointId());
        assertThat(optionalStartPoint).isPresent();
        assertThat(eventStartPointResponse.username()).isEqualTo(eventRequest.username());
        assertThat(optionalStartPoint.get().getStartPointId()).isEqualTo(eventStartPointResponse.startPointId());
        assertThat(optionalStartPoint.get().getName()).isEqualTo(eventRequest.startPoint());
        assertThat(optionalStartPoint.get().getUser()).isEqualTo(user);
        assertThat(optionalStartPoint.get().getAddress().getAddress()).isEqualTo(eventRequest.address());
        assertThat(optionalStartPoint.get().getAddress().getRoadAddress()).isEqualTo(eventRequest.roadAddress());
        assertThat(optionalStartPoint.get().getLocation().getRoadLongitude()).isEqualTo(eventRequest.longitude());
        assertThat(optionalStartPoint.get().getLocation().getRoadLatitude()).isEqualTo(eventRequest.latitude());
        assertThat(optionalStartPoint.get().getGuestId()).isNull();
        assertThat(optionalEvent.get().getEventName()).isEqualTo(eventRequest.eventName());
        assertThat(optionalEvent.get().getEventDateTime()).isEqualTo(eventRequest.toDateTime());
    }

    @Test
    @Transactional
    void 모임_수정_후_캐시와_모임경로데이터_검증() {
        //given
        cachedRouteRepository.save(eventWithRoute.getEventId(), meetingPointRouteGroups);

        //when
        eventService.updateEvent(eventWithRoute.getEventId(), updateEventRequest);

        //then
        Event updatedEvent = eventReader.read(eventWithRoute.getEventId());
        MeetingPointRouteGroups cache = cachedRouteRepository.findByEventId(eventWithRoute.getEventId()).orElseThrow();

        assertThat(cache)
                .usingRecursiveComparison()
                .isEqualTo(updatedEvent.getRoute());
    }

    @Test
    @Transactional
    void 모임_삭제_후_캐시와_모임경로데이터_검증() {
        //given
        cachedRouteRepository.save(eventWithRoute.getEventId(), meetingPointRouteGroups);

        Cache cache = cacheManager.getCache("routeDetails");
        assertThat(cache.get(eventWithRoute.getEventId())).isNotNull();

        //when
        eventService.deleteEvent(eventWithRoute.getEventId());

        //then
        assertThat(eventRepository.existsById(eventWithRoute.getEventId())).isFalse();
        assertThat(cache.get(eventWithRoute.getEventId())).isNull();
    }
}
