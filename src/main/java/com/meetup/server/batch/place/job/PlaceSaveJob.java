package com.meetup.server.batch.place.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.meetup.server.global.clients.google.place.GoogleFieldMask;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoClient;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoRequest;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoResponse;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextClient;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextRequest;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextResponse;
import com.meetup.server.global.clients.kakao.local.CategoryGroupCode;
import com.meetup.server.global.clients.kakao.local.KakaoLocalCategoryClient;
import com.meetup.server.global.clients.kakao.local.KakaoLocalRequest;
import com.meetup.server.global.clients.kakao.local.KakaoLocalResponse;
import com.meetup.server.global.clients.kakao.local.KakaoLocalResponse.KakaoSearchResponse;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.type.PlaceCategory;
import com.meetup.server.place.domain.value.GoogleReview;
import com.meetup.server.place.domain.value.Image;
import com.meetup.server.place.domain.value.OpeningHour;
import com.meetup.server.place.exception.PlaceErrorType;
import com.meetup.server.place.exception.PlaceException;
import com.meetup.server.place.implement.PlaceImageUploader;
import com.meetup.server.place.persistence.PlaceRepository;
import com.meetup.server.startpoint.domain.type.Location;
import com.meetup.server.subway.domain.Subway;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PlaceSaveJob {

    private final String JOB_NAME = this.getClass().getSimpleName();

    private final JobLauncher jobLauncher;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final PlaceRepository placeRepository;
    private final KakaoLocalCategoryClient kakaoLocalCategoryClient;
    private final GoogleSearchTextClient googleSearchTextClient;
    private final GooglePhotoClient googlePhotoClient;
    private final ObjectMapper objectMapper;
    private final PlaceImageUploader placeImageUploader;

    //    @Scheduled(cron = "0 0 3 1 * ?")
    public void savePlaceJobScheduler(int page) {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("time", new Date())
                .addLong("page", (long) page)
                .toJobParameters();
        try {
            jobLauncher.run(savePlaceJob(), jobParameters);
        } catch (Exception e) {
            log.error(JOB_NAME, e);
        }
    }

    @Bean
    public Job savePlaceJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(savePlaceStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step savePlaceStep(@Value("#{jobParameters[page]}") Long page) {
        return new StepBuilder("savePlaceStep", jobRepository)
                .<Subway, List<Place>>chunk(5, platformTransactionManager)
                .reader(subwayReader())
                .processor(convertToPlace(page))
                .writer(placeWriter())
                .build();
    }

    private ItemReader<Subway> subwayReader() {
        return new JpaPagingItemReaderBuilder<Subway>()
                .name("subwayReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Subway s ORDER BY s.subwayId")
                .pageSize(5)
                .build();
    }

    private ItemProcessor<Subway, List<Place>> convertToPlace(Long page) {
        return subway -> {
            List<Place> places = new ArrayList<>();
            KakaoLocalResponse kakaoLocalResponse = fetchKakaoPlaces(subway, page);

            for (KakaoSearchResponse kakaoSearchResponse : kakaoLocalResponse.getKakaoSearchResponses()) {
                try {
                    if (placeRepository.existsByKakaoPlaceId(kakaoSearchResponse.getId())) {
                        log.info("이미 존재하는 장소, 건너뜀: {}", kakaoSearchResponse.getPlaceName());
                        continue;
                    }

                    Place place = createPlace(kakaoSearchResponse);
                    places.add(place);
                } catch (Exception e) {
                    log.warn("Place 생성 실패: {}", e.getMessage());
                }
            }

            return places;
        };
    }

    private ItemWriter<List<Place>> placeWriter() {
        return items -> {
            List<Place> flatList = items.getItems().stream()
                    .flatMap(List::stream)
                    .toList();
            placeRepository.saveAll(flatList);
        };
    }

    private KakaoLocalResponse fetchKakaoPlaces(Subway subway, Long page) {
        return kakaoLocalCategoryClient.sendRequest(KakaoLocalRequest.builder()
                .categoryGroupCode(CategoryGroupCode.CE7)
                .x(String.valueOf(subway.getLocation().getRoadLongitude()))
                .y(String.valueOf(subway.getLocation().getRoadLatitude()))
                .radius(500)
                .page(page.intValue())
                .size(10)
                .build());
    }

    private Place createPlace(KakaoSearchResponse kakaoSearchResponse) throws JsonProcessingException {
        UUID placeId = UuidCreator.getTimeOrderedEpoch();

        GoogleSearchTextResponse.Place googlePlace = fetchGooglePlace(kakaoSearchResponse.getPlaceName());
        String photoUri = uploadPlaceImage(googlePlace, placeId);

        double longitude = Double.parseDouble(kakaoSearchResponse.getX());
        double latitude = Double.parseDouble(kakaoSearchResponse.getY());

        return buildPlace(placeId, kakaoSearchResponse, googlePlace, photoUri, longitude, latitude);
    }

    private GoogleSearchTextResponse.Place fetchGooglePlace(String placeName) {
        GoogleSearchTextResponse googleSearchTextResponse = googleSearchTextClient.sendRequest(
                GoogleSearchTextRequest.from(placeName),
                GoogleFieldMask.ALL
        );

        return googleSearchTextResponse.places().getFirst();
    }

    private String uploadPlaceImage(GoogleSearchTextResponse.Place googlePlace, UUID placeId) {
        String photoName = googlePlace.photos().getFirst().name();

        GooglePhotoResponse googlePhotoResponse = fetchGooglePhoto(photoName);
        if (googlePhotoResponse == null || googlePhotoResponse.photoUri() == null) {
            throw new PlaceException(PlaceErrorType.PLACE_IMAGE_UPLOAD_FAILED);
        }

        return placeImageUploader.uploadImage(googlePhotoResponse.photoUri(), placeId);
    }

    private Place buildPlace(UUID placeId,
                             KakaoSearchResponse kakaoSearchResponse,
                             GoogleSearchTextResponse.Place googlePlace,
                             String photoUri,
                             double longitude,
                             double latitude) throws JsonProcessingException {
        return Place.builder()
                .id(placeId)
                .kakaoPlaceId(kakaoSearchResponse.getId())
                .googlePlaceId(googlePlace.id())
                .category(PlaceCategory.CAFE)
                .name(kakaoSearchResponse.getPlaceName())
                .googleRating(googlePlace.rating())
                .images(List.of(Image.from(photoUri)))
                .openingHours(
                        Optional.ofNullable(googlePlace.regularOpeningHours())
                                .map(openingHours -> openingHours.periods().stream().map(OpeningHour::from).toList())
                                .orElseGet(List::of)
                )
                .googleReviews(
                        Optional.ofNullable(googlePlace.reviews())
                                .map(reviews -> reviews.stream().map(GoogleReview::from).toList())
                                .orElseGet(List::of)
                )
                .location(Location.of(longitude, latitude))
                .point(CoordinateUtil.createPoint(longitude, latitude))
                .rawJson(objectMapper.writeValueAsString(googlePlace))
                .build();
    }

    private GooglePhotoResponse fetchGooglePhoto(String photoName) {
        GooglePhotoRequest googlePhotoRequest = GooglePhotoRequest.builder()
                .name(photoName)
                .maxHeightPx(1200)
                .maxWidthPx(1200)
                .build();
        return googlePhotoClient.sendRequest(googlePhotoRequest);
    }
}
