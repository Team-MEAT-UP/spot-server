package com.meetup.server.global.clients.kakao.local;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KakaoLocalCategoryClient {

    private final RestClient kakaoLocalRestClient;

    public KakaoLocalResponse sendRequest(KakaoLocalRequest request) {
        return kakaoLocalRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/category.json")
                        .queryParam("category_group_code", request.categoryGroupCode())
                        .queryParamIfPresent("x", Optional.ofNullable(request.x()))
                        .queryParamIfPresent("y", Optional.ofNullable(request.y()))
                        .queryParamIfPresent("radius", Optional.ofNullable(request.radius()))
                        .queryParamIfPresent("rect", Optional.ofNullable(request.rect()))
                        .queryParamIfPresent("page", Optional.ofNullable(request.page()))
                        .queryParamIfPresent("size", Optional.ofNullable(request.size()))
                        .queryParamIfPresent("sort", Optional.ofNullable(request.sort()))
                        .build())
                .retrieve()
                .body(KakaoLocalResponse.class);
    }
}
