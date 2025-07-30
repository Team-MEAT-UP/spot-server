package com.meetup.server.global.clients.google.place;

import com.meetup.server.global.clients.google.place.photo.GooglePhotoClient;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoRequest;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoResponse;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextClient;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextRequest;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextResponse;
import com.meetup.server.support.IntegrationTestContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("API 호출 시, 과금 가능성으로 인한 테스트 비활성화")
class GooglePlaceClientTest extends IntegrationTestContainer {

    @Autowired
    private GoogleSearchTextClient googleSearchTextClient;

    @Autowired
    private GooglePhotoClient googlePhotoClient;

    @Test
    void 구글_텍스트_검색_요청에_성공하고_장소_사진_조회에_성공한다() {
        GoogleSearchTextRequest request = GoogleSearchTextRequest.from("스타벅스 방배점");
        GoogleSearchTextResponse response = googleSearchTextClient.sendRequest(request, GoogleFieldMask.ALL);

        assertNotNull(response);
        assertNotNull(response.places().getFirst().id());

        String name = response.places().getFirst().photos().getFirst().name();
        assertNotNull(name);

        GooglePhotoRequest photoRequest = GooglePhotoRequest.builder()
                .name(name)
                .maxHeightPx(1600)
                .maxWidthPx(1600)
                .build();

        GooglePhotoResponse photoResponse = googlePhotoClient.sendRequest(photoRequest);

        assertNotNull(photoResponse);
        assertNotNull(photoResponse.photoUri());
    }
}

