package com.meetup.server.global.clients.simplestorage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record SimpleStorageProperties(
        String bucketName,
        String accessKey,
        String secretKey,
        String region
) {
}
