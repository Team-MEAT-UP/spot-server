package com.meetup.server.event.implement;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.exception.EventException;
import com.meetup.server.event.infrastructure.jpa.EventRepository;
import com.meetup.server.fixture.EventFixture;
import com.meetup.server.fixture.StartPointFixture;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.infrastructure.jpa.StartPointRepository;
import com.meetup.server.support.IntegrationTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventValidatorTest extends IntegrationTestContainer {

    private static final int MAX_START_POINTS = 8;

    @Autowired
    private EventValidator eventValidator;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private StartPointRepository startPointRepository;

    @Test
    void 최대_출발지_개수를_초과하면_출발지_등록에_실패한다() {
        Event event = eventRepository.save(EventFixture.getEvent());

        List<StartPoint> startPoints = new ArrayList<>();
        for (int i = 0; i < MAX_START_POINTS; i++) {
            startPoints.add(StartPointFixture.getStartPoint(event, null));
        }
        startPointRepository.saveAll(startPoints);

        assertThatThrownBy(() -> eventValidator.validateEventIsNotFull(event))
                .isInstanceOf(EventException.class);
    }
}
