package com.meetup.server.place.implement;

import com.meetup.server.global.clients.simplestorage.SimpleStorageUploader;
import com.meetup.server.global.util.ImageConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PlaceImageUploader {

    private final SimpleStorageUploader simpleStorageUploader;

    public static final String CONTENT_TYPE_PNG = "image/png";

    public String uploadImage(String imageUrl, String placeId) {
        byte[] imageData = ImageConverter.downloadImage(imageUrl);
        String objectKey = simpleStorageUploader.generateObjectKey(placeId, 1, imageUrl);
        return simpleStorageUploader.uploadImage(imageData, objectKey, CONTENT_TYPE_PNG);
    }
}
