package com.meetup.server.place.infrastructure.jpa.init;

import com.meetup.server.global.support.DummyDataInit;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.type.PlaceCategory;
import com.meetup.server.place.infrastructure.jpa.PlaceRepository;
import com.meetup.server.startpoint.domain.type.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Profile("local")
@Order(2)
@DummyDataInit
public class PlaceDummy implements ApplicationRunner {

    private final PlaceRepository placeRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (placeRepository.count() > 0) {
            log.debug("[REVIEW]더미 데이터 존재");
        } else {
            List<Place> placeList = new ArrayList<>();

            placeList.add(Place.builder()
                    .name("스타벅스 강남점")
                    .googlePlaceId("GOOGLE_PLACE_ID")
                    .kakaoPlaceId("KAKAO_PLACE_ID")
                    .location(Location.of(37.4979, 127.0276))
                    .category(PlaceCategory.CAFE)
                    .build());

            placeRepository.saveAll(placeList);
        }
    }
}
