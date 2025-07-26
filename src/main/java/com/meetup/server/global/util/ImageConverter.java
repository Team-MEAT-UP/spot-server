package com.meetup.server.global.util;

import com.meetup.server.global.support.error.GlobalErrorType;
import com.meetup.server.global.support.error.GlobalException;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

@Slf4j
public class ImageConverter {

    private static final String IMAGE_FORMAT = "png";

    public static byte[] downloadImage(String url) {
        try {
            URL imageUrl = URI.create(url).toURL();
            return convertUrlToBytes(imageUrl, url);

        } catch (Exception e) {
            log.error("이미지 변환 중 예외 발생: {}", e.getMessage(), e);
            throw new GlobalException(GlobalErrorType.IMAGE_CONVERSION_ERROR);
        }
    }

    private static byte[] convertUrlToBytes(URL imageUrl, String originalUrl) throws IOException {
        try (InputStream inputStream = imageUrl.openStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                log.error("이미지 변환 중 URL 처리 실패: {}", originalUrl);
                throw new GlobalException(GlobalErrorType.IMAGE_CONVERSION_ERROR);
            }

            ImageIO.write(image, IMAGE_FORMAT, outputStream);
            return outputStream.toByteArray();
        }
    }
}
