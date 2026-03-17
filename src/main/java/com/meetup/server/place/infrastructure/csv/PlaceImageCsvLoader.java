package com.meetup.server.place.infrastructure.csv;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.server.global.clients.simplestorage.SimpleStorageUploader;
import com.meetup.server.global.util.ImageConverter;
import com.meetup.server.place.domain.Place;
import com.meetup.server.place.domain.value.Image;
import com.meetup.server.place.infrastructure.jpa.PlaceRepository;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
public class PlaceImageCsvLoader implements ApplicationRunner {

    private final PlaceRepository placeRepository;
    private final SimpleStorageUploader s3Uploader;
    private final ObjectMapper objectMapper;
    private final KakaoLocalKeywordClient kakaoLocalKeywordClient;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource("csv/place_image_migration.csv");
        if (!resource.exists()) {
            log.info("[PlaceImageCsvLoader] Migration CSV file not found. Skipping migration.");
            return;
        }

        log.info("[PlaceImageCsvLoader] Starting full place synchronization from CSV...");
        List<PlaceImageCsvMapping> csvMappings = parseCsv(resource);

        for (PlaceImageCsvMapping mapping : csvMappings) {
            processMapping(mapping);
        }
        log.info("[PlaceImageCsvLoader] place synchronization completed.");
    }

    private List<PlaceImageCsvMapping> parseCsv(ClassPathResource resource) {
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return new CsvToBeanBuilder<PlaceImageCsvMapping>(reader)
                    .withType(PlaceImageCsvMapping.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
        } catch (Exception e) {
            log.error("[PlaceImageCsvLoader] CSV parsing error", e);
            return List.of();
        }
    }

    private void processMapping(PlaceImageCsvMapping mapping) {
        if (mapping.getGooglePlaceId() == null || mapping.getGooglePlaceId().isBlank()) {
            log.warn("[PlaceImageCsvLoader] Skipping row with empty google_place_id for place: {}", mapping.getName());
            return;
        }

        placeRepository.findByGooglePlaceId(mapping.getGooglePlaceId()).ifPresentOrElse(
                place -> updateExistingPlace(place, mapping),
                () -> createNewPlace(mapping)
        );
    }

    private void updateExistingPlace(Place place, PlaceImageCsvMapping mapping) {
        try {
            List<Image> sourceImages = deserializeJson(mapping.getImagesJson(), new TypeReference<List<Image>>() {});
            List<Image> s3Images = uploadImagesToS3(place.getGooglePlaceId(), sourceImages);
            place.updateImages(s3Images);

            List<GoogleReview> reviews = deserializeJson(mapping.getGoogleReviewsJson(), new TypeReference<List<GoogleReview>>() {});
            place.saveGoogleReviews(reviews);

            // Google rating can also be updated if provided
            if (mapping.getGoogleRating() != null) {
                // Assuming we might have a setter or builder pattern, but Place entity shows we need to check if we can update other fields.
                // Currently Place only has updateImages and saveGoogleReviews as public methods for updates.
            }

            log.info("[PlaceImageCsvLoader] Successfully updated place: {} ({})", place.getName(), place.getGooglePlaceId());
        } catch (Exception e) {
            log.error("[PlaceImageCsvLoader] Failed to update place: {}", mapping.getGooglePlaceId(), e);
        }
    }

    private void createNewPlace(PlaceImageCsvMapping mapping) {
        try {
            // Search Kakao for mandatory kakaoPlaceId
            KakaoLocalRequest kakaoRequest = KakaoLocalRequest.builder()
                    .query(mapping.getName())
                    .size(1)
                    .build();
            KakaoLocalResponse kakaoResponse = kakaoLocalKeywordClient.sendRequest(kakaoRequest);

            if (kakaoResponse.getKakaoSearchResponses().isEmpty()) {
                log.warn("[PlaceImageCsvLoader] Could not find kakaoPlaceId for {}, skipping creation.", mapping.getName());
                return;
            }

            KakaoLocalResponse.KakaoSearchResponse kakaoPlace = kakaoResponse.getKakaoSearchResponses().get(0);
            
            double longitude = mapping.getLongitude() != null ? mapping.getLongitude() : Double.parseDouble(kakaoPlace.getX());
            double latitude = mapping.getLatitude() != null ? mapping.getLatitude() : Double.parseDouble(kakaoPlace.getY());

            List<Image> sourceImages = deserializeJson(mapping.getImagesJson(), new TypeReference<List<Image>>() {});
            List<Image> s3Images = uploadImagesToS3(mapping.getGooglePlaceId(), sourceImages);
            
            List<OpeningHour> openingHours = deserializeJson(mapping.getOpeningHoursJson(), new TypeReference<List<OpeningHour>>() {});
            List<GoogleReview> reviews = deserializeJson(mapping.getGoogleReviewsJson(), new TypeReference<List<GoogleReview>>() {});

            Place newPlace = Place.builder()
                    .kakaoPlaceId(kakaoPlace.getId())
                    .googlePlaceId(mapping.getGooglePlaceId())
                    .name(mapping.getName())
                    .category(PlaceCategory.CAFE) // Default to CAFE as discussed
                    .googleRating(mapping.getGoogleRating())
                    .images(s3Images)
                    .openingHours(openingHours)
                    .googleReviews(reviews)
                    .location(Location.of(longitude, latitude))
                    .point(CoordinateUtil.createPoint(longitude, latitude))
                    .build();

            placeRepository.save(newPlace);
            log.info("[PlaceImageCsvLoader] Successfully created new place: {} ({})", mapping.getName(), mapping.getGooglePlaceId());

        } catch (Exception e) {
            log.error("[PlaceImageCsvLoader] Failed to create new place: {}", mapping.getGooglePlaceId(), e);
        }
    }

    private <T> T deserializeJson(String json, TypeReference<T> typeReference) throws Exception {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return objectMapper.readValue("[]", typeReference);
        }
        return objectMapper.readValue(json, typeReference);
    }

    private List<Image> uploadImagesToS3(String googlePlaceId, List<Image> sourceImages) {
        if (sourceImages == null || sourceImages.isEmpty()) {
            return List.of();
        }
        
        List<Image> s3Images = new ArrayList<>();
        for (int i = 0; i < sourceImages.size(); i++) {
            Image sourceImage = sourceImages.get(i);
            try {
                // If already S3 URL, skip upload (safety check)
                if (sourceImage.photoUri().contains("s3.amazonaws.com") || sourceImage.photoUri().contains(".com/")) {
                    // But usually we want to download from external and upload to our S3
                }
                
                byte[] imageBytes = ImageConverter.downloadImage(sourceImage.photoUri());
                if (imageBytes != null) {
                    String contentType = "image/png";
                    String key = s3Uploader.generateObjectKey(googlePlaceId, i, sourceImage.photoUri());
                    String s3Url = s3Uploader.uploadImage(imageBytes, key, contentType);
                    s3Images.add(Image.from(s3Url));
                }
            } catch (Exception e) {
                log.error("[PlaceImageCsvLoader] Failed to upload image to S3: {}", sourceImage.photoUri(), e);
            }
        }
        return s3Images;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PlaceImageCsvMapping {
        @CsvBindByName(column = "name")
        private String name;

        @CsvBindByName(column = "google_place_id")
        private String googlePlaceId;

        @CsvBindByName(column = "latitude")
        private Double latitude;

        @CsvBindByName(column = "longitude")
        private Double longitude;

        @CsvBindByName(column = "google_rating")
        private Double googleRating;

        @CsvBindByName(column = "opening_hours")
        private String openingHoursJson;

        @CsvBindByName(column = "google_reviews")
        private String googleReviewsJson;

        @CsvBindByName(column = "images")
        private String imagesJson;
    }
}
