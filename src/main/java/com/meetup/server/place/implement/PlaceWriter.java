package com.meetup.server.place.implement;

import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.value.GoogleReview;
import com.meetup.server.place.persistence.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaceWriter {

    private final PlaceRepository placeRepository;

    public void saveGoogleReviews(Place place, List<GoogleReview> translatedReviews) {
        place.saveGoogleReviews(translatedReviews);
        placeRepository.save(place);
    }
}
