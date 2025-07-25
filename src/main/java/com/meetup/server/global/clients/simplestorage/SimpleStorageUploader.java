package com.meetup.server.global.clients.simplestorage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.meetup.server.global.support.error.GlobalErrorType;
import com.meetup.server.global.support.error.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SimpleStorageUploader {

    private final AmazonS3 amazonS3;
    private final SimpleStorageProperties properties;

    public String uploadImage(byte[] data, String key, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(data.length);
        metadata.setContentType(contentType);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            amazonS3.putObject(new PutObjectRequest(properties.bucketName(), key, inputStream, metadata));
            return amazonS3.getUrl(properties.bucketName(), key).toString();

        } catch (Exception e) {
            throw new GlobalException(GlobalErrorType.IMAGE_UPLOAD_ERROR);
        }
    }

    public String generateObjectKey(String placeId, int index, String originalUrl) {
        String extension = extractExtensionOrDefault(originalUrl);
        String normalizedPlaceId = placeId.startsWith("places/") ? placeId.substring("places/".length()) : placeId;
        return String.format("places/%s/photos/%d%s", normalizedPlaceId, index, extension);
    }

    private String extractExtensionOrDefault(String url) {
        int lastDotIndex = url.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex > url.lastIndexOf('/')) {
            return url.substring(lastDotIndex);
        }
        return ".png";
    }
}
