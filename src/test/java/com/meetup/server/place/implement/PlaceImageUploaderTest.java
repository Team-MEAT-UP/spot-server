package com.meetup.server.place.implement;

import com.meetup.server.fixture.PlaceFixture;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoClient;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoRequest;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoResponse;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextClient;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextRequest;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextResponse;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.exception.PlaceErrorType;
import com.meetup.server.place.exception.PlaceException;
import com.meetup.server.place.persistence.PlaceRepository;
import com.meetup.server.support.IntegrationTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PlaceImageUploaderTest extends IntegrationTestContainer {

    @Autowired
    private PlaceImageUploader placeImageUploader;

    @Autowired
    private GooglePhotoClient googlePhotoClient;

    @Autowired
    private GoogleSearchTextClient googleSearchTextClient;

    @Autowired
    private PlaceRepository placeRepository;

    @BeforeEach
    void setUp() {
        Place place = PlaceFixture.getPlace();
        placeRepository.save(place);
    }

    public String uploadImageByPlaceName(Place place, Integer maxHeightPx, Integer maxWidthPx) {
        GoogleSearchTextRequest searchRequest = GoogleSearchTextRequest.from(place.getName());
        GoogleSearchTextResponse searchResponse = googleSearchTextClient.sendRequest(searchRequest);

        if (searchResponse.places().isEmpty()) {
            throw new PlaceException(PlaceErrorType.PLACE_NOT_FOUND);
        }

        GoogleSearchTextResponse.Place latestPlace = searchResponse.places().getFirst();

        if (latestPlace.photos() == null || latestPlace.photos().isEmpty()) {
            throw new PlaceException(PlaceErrorType.PLACE_IMAGE_UPLOAD_FAILED);
        }

        String latestPhotoName = latestPlace.photos().getFirst().name();

        GooglePhotoRequest photoRequest = GooglePhotoRequest.builder()
                .name(latestPhotoName)
                .maxHeightPx(maxHeightPx)
                .maxWidthPx(maxWidthPx)
                .build();

        GooglePhotoResponse photoResponse = googlePhotoClient.sendRequest(photoRequest);

        if (photoResponse == null || photoResponse.photoUri() == null) {
            throw new PlaceException(PlaceErrorType.PLACE_IMAGE_UPLOAD_FAILED);
        }

        return placeImageUploader.uploadImage(photoResponse.photoUri(), place.getId());
    }

    //@Disabled("API 호출 시, 과금 가능성으로 인한 테스트 비활성화")
    @Test
    void 구글_장소_이름으로_이미지를_S3에_업로드한다() {
        Place place = placeRepository.findAll().getFirst();

        Integer maxHeightPx = 1200;
        Integer maxWidthPx = 1200;

        String imageUrl = uploadImageByPlaceName(place, maxHeightPx, maxWidthPx);

        assert imageUrl != null : "이미지 업로드에 실패했습니다.";

        System.out.println("업로드된 이미지 URL: " + imageUrl);
        assertThat(imageUrl).isNotNull().isNotBlank();
    }
}
