package com.meetup.server.event.implement.route;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.response.route.MeetingPointResult;
import com.meetup.server.fixture.EventFixture;
import com.meetup.server.fixture.ParkingLotFixture;
import com.meetup.server.fixture.SubwayFixture;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.parkinglot.domain.ParkingLot;
import com.meetup.server.parkinglot.infrastructure.jpa.ParkingLotRepository;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.startpoint.domain.type.Address;
import com.meetup.server.startpoint.domain.type.Location;
import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.infrastructure.jpa.SubwayRepository;
import com.meetup.server.support.IntegrationTestContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

@Disabled("API 호출 시, 과금 가능성으로 인한 테스트 비활성화")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RouteProcessorTest extends IntegrationTestContainer {

    @Autowired
    RouteProcessor routeProcessor;

    @Autowired
    SubwayRepository subwayRepository;

    @Autowired
    ParkingLotRepository parkingLotRepository;

    Event event;
    Subway subway;
    List<StartPoint> transitStartPoints;
    List<StartPoint> drivingStartPoints;

    @BeforeAll
    void setUp() {
        event = EventFixture.getEvent();
        subway = subwayRepository.save(SubwayFixture.getSubway());

        ParkingLot parkingLot = ParkingLotFixture.getParkingLot();
        parkingLotRepository.save(parkingLot);

        List<double[]> transitCoordinates = List.of(
                new double[]{37.49808633653005, 127.02800140627488},
                new double[]{37.3782271209233, 126.852937037394},
                new double[]{37.603394782316634, 127.02506019737066},
                new double[]{37.4941629743516, 126.724277577653}
        );

        List<double[]> drivingCoordinates = List.of(
                new double[]{37.64552551128486, 127.01431410391658},
                new double[]{37.55406888733184, 126.97070335253385},
                new double[]{37.71499518010759, 127.49053682305495},
                new double[]{37.5345613066561, 126.99580922812}
        );

        transitStartPoints = transitCoordinates.stream()
                .map(transitCoordinate -> StartPoint.builder()
                        .event(event)
                        .name("Transit StartPoint")
                        .isUser(false)
                        .isTransit(true)
                        .address(Address.of("Test Address", "Test Road Address"))
                        .location(Location.of(transitCoordinate[1], transitCoordinate[0]))
                        .point(CoordinateUtil.createPoint(transitCoordinate[1], transitCoordinate[0]))
                        .build())
                .toList();

        drivingStartPoints = drivingCoordinates.stream()
                .map(drivingCoordinate -> StartPoint.builder()
                        .event(event)
                        .name("Driving StartPoint")
                        .isUser(false)
                        .isTransit(false)
                        .address(Address.of("Test Address", "Test Road Address"))
                        .location(Location.of(drivingCoordinate[1], drivingCoordinate[0]))
                        .point(CoordinateUtil.createPoint(drivingCoordinate[1], drivingCoordinate[0]))
                        .build())
                .toList();
    }

    @DisplayName("출발지 개수에 따른 경로 생성 소요 시간을 측정한다.")
    @ParameterizedTest
    @ValueSource(ints = {2, 4, 6, 8})
    void testBuildRouteGroupsPerformance(int size) {
        int transitSize = size / 2;
        int drivingSize = size - transitSize;

        List<StartPoint> testStartPoints = Stream.concat(
                transitStartPoints.stream().limit(transitSize),
                drivingStartPoints.stream().limit(drivingSize)
        ).toList();

        MeetingPointResult coordinateResult = MeetingPointResult.of(event, testStartPoints, subway);
        MeetingPointResult popularResult = MeetingPointResult.of(event, testStartPoints, subway);

        long startTime = System.nanoTime();
        routeProcessor.buildRouteGroup(coordinateResult);
        routeProcessor.buildRouteGroup(popularResult);
        long endTime = System.nanoTime();
        
        System.out.printf("출발지 개수: %d → (좌표+인기 2건 순차적) 소요 시간: %d ms%n", size, (endTime - startTime) / 1_000_000);
    }
}
