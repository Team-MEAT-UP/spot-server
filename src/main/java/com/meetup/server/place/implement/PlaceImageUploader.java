package com.meetup.server.place.implement;

import com.meetup.server.global.clients.simplestorage.SimpleStorageUploader;
import com.meetup.server.global.util.ImageConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class PlaceImageUploader {

    private final SimpleStorageUploader simpleStorageUploader;

    public static final String CONTENT_TYPE_PNG = "image/png";

    public String uploadImage(String imageUrl, UUID placeId) {
        byte[] imageData = ImageConverter.downloadImage(imageUrl);
        String objectKey = simpleStorageUploader.generateObjectKey(placeId.toString(), 1, imageUrl);
        return simpleStorageUploader.uploadImage(imageData, objectKey, CONTENT_TYPE_PNG);
    }
}
