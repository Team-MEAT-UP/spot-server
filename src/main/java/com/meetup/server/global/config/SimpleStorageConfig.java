package com.meetup.server.global.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.meetup.server.global.clients.simplestorage.SimpleStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SimpleStorageConfig {

    private final SimpleStorageProperties properties;

    @Bean
    public AmazonS3 amazonS3() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(properties.accessKey(), properties.secretKey());
        return AmazonS3ClientBuilder.standard()
                .withRegion(properties.region())
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}
