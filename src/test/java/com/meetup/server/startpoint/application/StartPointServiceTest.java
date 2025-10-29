package com.meetup.server.startpoint.application;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.event.implement.EventReader;
import com.meetup.server.event.infrastructure.jpa.EventRepository;
import com.meetup.server.event.infrastructure.redis.CachedRouteRepository;
import com.meetup.server.fixture.EventFixture;
import com.meetup.server.fixture.StartPointFixture;
import com.meetup.server.fixture.UserFixture;
import com.meetup.server.place.infrastructure.jpa.PlaceRepository;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.dto.request.StartPointRequest;
import com.meetup.server.startpoint.infrastructure.jpa.StartPointRepository;
import com.meetup.server.support.IntegrationTestContainer;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.infrastructure.jpa.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StartPointServiceTest extends IntegrationTestContainer {

    @Autowired
    private StartPointService startPointService;

    @Autowired
    private StartPointRepository startPointRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private CachedRouteRepository cachedRouteRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EventReader eventReader;

    @Autowired
    private EntityManager entityManager;

    private Event event;
    private StartPointRequest startPointRequest;
    private User user;
    private User newUser;
    private UUID guestId;
    private StartPoint startPoint;
    private Event eventWithRoute;
    private MeetingPointRouteGroups meetingPointRouteGroups;

    @BeforeEach
    void setUp() {
        event = eventRepository.save(EventFixture.getEvent());
        startPointRequest = StartPointFixture.getStartPointRequest();
        user = userRepository.save(UserFixture.getUser());
        newUser = userRepository.save(UserFixture.getNewUser());
        guestId = UUID.randomUUID();
        eventWithRoute = EventFixture.getEventWithRoute();
        placeRepository.save(eventWithRoute.getPlace());
        eventWithRoute = eventRepository.save(eventWithRoute);
        meetingPointRouteGroups = EventFixture.getMeetingPointRouteGroups();
        startPoint = startPointRepository.save(StartPointFixture.getStartPoint(eventWithRoute, newUser));
        cachedRouteRepository.save(eventWithRoute.getEventId(), meetingPointRouteGroups);
    }

    @Test
    @Transactional
    void 비로그인_사용자가_출발지를_저장한다() {
        EventStartPointResponse eventStartPointResponse = startPointService.createStartPoint(
                event.getEventId(),
                null,
                guestId,
                startPointRequest
        );

        assertThat(event.getEventId()).isEqualTo(eventStartPointResponse.eventId());

        Optional<StartPoint> optionalStartPoint = startPointRepository.findById(eventStartPointResponse.startPointId());
        assertThat(optionalStartPoint).isPresent();
        assertThat(optionalStartPoint.get().getStartPointId()).isEqualTo(eventStartPointResponse.startPointId());
        assertThat(optionalStartPoint.get().getUser()).isNull();
        assertThat(optionalStartPoint.get().getGuestId()).isEqualTo(guestId);
        assertThat(optionalStartPoint.get().isTransit()).isEqualTo(startPointRequest.isTransit());
    }

    @Test
    @Transactional
    void 로그인_사용자가_출발지를_저장한다() {
        EventStartPointResponse eventStartPointResponse = startPointService.createStartPoint(
                event.getEventId(),
                user.getUserId(),
                null,
                startPointRequest
        );

        assertThat(event.getEventId()).isEqualTo(eventStartPointResponse.eventId());

        Optional<StartPoint> optionalStartPoint = startPointRepository.findById(eventStartPointResponse.startPointId());
        assertThat(optionalStartPoint).isPresent();
        assertThat(optionalStartPoint.get().getStartPointId()).isEqualTo(eventStartPointResponse.startPointId());
        assertThat(optionalStartPoint.get().getUser()).isEqualTo(user);
        assertThat(optionalStartPoint.get().getGuestId()).isNull();
        assertThat(optionalStartPoint.get().isTransit()).isEqualTo(startPointRequest.isTransit());
    }

    @Test
    @Transactional
    void 탈퇴한_사용자_출발지_삭제_확인() {
        User user = UserFixture.getUser();
        userRepository.save(user);

        StartPoint startPoint = StartPointFixture.getStartPoint(event, user);
        startPointRepository.save(startPoint);

        startPointRepository.deleteAllByUser(user);

        UUID startPointId = startPoint.getStartPointId();
        assertThat(startPointRepository.existsById(startPointId)).isFalse();
    }

    @Test
    @Transactional
    void 출발지_생성_후_캐시와_모임경로데이터_삭제_검증() {
        // given
        Cache cache = cacheManager.getCache("routeDetails");
        assertThat(cache.get(eventWithRoute.getEventId())).isNotNull();

        //when
        startPointService.createStartPoint(
                eventWithRoute.getEventId(),
                user.getUserId(),
                null,
                startPointRequest
        );

        entityManager.flush();
        entityManager.clear();

        //then
        assertThat(eventReader.read(eventWithRoute.getEventId()).getRoutes()).isNull();
        assertThat(cache.get(eventWithRoute.getEventId())).isNull();
    }

    @Test
    @Transactional
    void 출발지_수정_후_캐시와_모임경로데이터_삭제_검증() {
        // given
        Cache cache = cacheManager.getCache("routeDetails");
        assertThat(cache.get(eventWithRoute.getEventId())).isNotNull();

        //when
        startPointService.updateStartPoint(
                eventWithRoute.getEventId(),
                startPoint.getStartPointId(),
                startPointRequest
        );

        entityManager.flush();
        entityManager.clear();

        //then
        assertThat(eventReader.read(eventWithRoute.getEventId()).getRoutes()).isNull();
        assertThat(cache.get(eventWithRoute.getEventId())).isNull();
    }

    @Test
    @Transactional
    void 출발지_삭제_후_캐시와_모임경로데이터_삭제_검증() {
        // given
        Cache cache = cacheManager.getCache("routeDetails");
        assertThat(cache.get(eventWithRoute.getEventId())).isNotNull();

        // when
        startPointService.deleteStartPoint(
                eventWithRoute.getEventId(),
                startPoint.getStartPointId()
        );

        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(eventReader.read(eventWithRoute.getEventId()).getRoutes()).isNull();
        assertThat(cache.get(eventWithRoute.getEventId())).isNull();
    }
}
