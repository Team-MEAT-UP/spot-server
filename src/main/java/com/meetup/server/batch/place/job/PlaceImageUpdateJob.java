package com.meetup.server.batch.place.job;

import com.meetup.server.global.clients.google.place.GoogleFieldMask;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoClient;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoRequest;
import com.meetup.server.global.clients.google.place.photo.GooglePhotoResponse;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextClient;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextRequest;
import com.meetup.server.global.clients.google.place.search.GoogleSearchTextResponse;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.exception.PlaceErrorType;
import com.meetup.server.place.exception.PlaceException;
import com.meetup.server.place.implement.PlaceImageUploader;
import com.meetup.server.place.infrastructure.jpa.PlaceRepository;
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
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PlaceImageUpdateJob {

    private final String JOB_NAME = this.getClass().getSimpleName();

    private final JobLauncher jobLauncher;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final PlaceRepository placeRepository;
    private final GoogleSearchTextClient googleSearchTextClient;
    private final GooglePhotoClient googlePhotoClient;
    private final PlaceImageUploader placeImageUploader;

    //    @Scheduled(cron = "0 0 3 1 * ?")
    public void updatePlaceImageJobScheduler() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("time", new Date())
                .toJobParameters();
        try {
            jobLauncher.run(updatePlaceImageJob(), jobParameters);
        } catch (Exception e) {
            log.error(JOB_NAME, e);
        }
    }

    @Bean
    public Job updatePlaceImageJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(updatePlaceImageStep())
                .build();
    }

    @Bean
    @JobScope
    public Step updatePlaceImageStep() {
        return new StepBuilder("updatePlaceImageStep", jobRepository)
                .<Place, Place>chunk(5, platformTransactionManager)
                .reader(placeReader())
                .processor(updatePlaceImageProcessor())
                .writer(updatePlaceImageWriter())
                .build();
    }

    private ItemReader<Place> placeReader() {
        return new JpaPagingItemReaderBuilder<Place>()
                .name("placeReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT p FROM Place p")
                .pageSize(5)
                .build();
    }

    private ItemProcessor<Place, Place> updatePlaceImageProcessor() {
        return place -> {
            try {
                String imageUrl = uploadImageByPlace(place);
                place.updateImage(imageUrl);
            } catch (Exception e) {
                log.warn("[PlaceImageUpdateJob] 장소 이미지 업데이트 실패 {}: {}", place.getId(), e.getMessage());
            }
            return place;
        };
    }

    private ItemWriter<Place> updatePlaceImageWriter() {
        return new RepositoryItemWriterBuilder<Place>()
                .repository(placeRepository)
                .build();
    }

    private String uploadImageByPlace(Place place) {
        GoogleSearchTextResponse.Place googlePlace = fetchGooglePlace(place);

        String googlePhotoName = googlePlace.photos().getFirst().name();
        GooglePhotoResponse photoResponse = fetchGooglePhoto(googlePhotoName);

        return placeImageUploader.uploadImage(photoResponse.photoUri(), place.getId());
    }

    private GoogleSearchTextResponse.Place fetchGooglePlace(Place place) {
        GoogleSearchTextRequest googleSearchTextRequest = GoogleSearchTextRequest.from(place.getName());
        GoogleSearchTextResponse googleSearchTextResponse = googleSearchTextClient.sendRequest(googleSearchTextRequest, GoogleFieldMask.PHOTOS);

        if (googleSearchTextResponse == null || googleSearchTextResponse.places().isEmpty()) {
            throw new PlaceException(PlaceErrorType.PLACE_NOT_FOUND);
        }

        GoogleSearchTextResponse.Place googlePlace = googleSearchTextResponse.places().getFirst();

        if (googlePlace.photos() == null || googlePlace.photos().isEmpty()) {
            throw new PlaceException(PlaceErrorType.PLACE_IMAGE_UPLOAD_FAILED);
        }

        return googlePlace;
    }

    private GooglePhotoResponse fetchGooglePhoto(String photoName) {
        GooglePhotoRequest googlePhotoRequest = GooglePhotoRequest.builder()
                .name(photoName)
                .maxHeightPx(1200)
                .maxWidthPx(1200)
                .build();

        GooglePhotoResponse googlePhotoResponse = googlePhotoClient.sendRequest(googlePhotoRequest);

        if (googlePhotoResponse == null || googlePhotoResponse.photoUri() == null) {
            throw new PlaceException(PlaceErrorType.PLACE_IMAGE_UPLOAD_FAILED);
        }

        return googlePhotoResponse;
    }
}
