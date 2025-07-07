package com.meetup.server.startpoint.implement;

import com.meetup.server.global.clients.kakao.local.KakaoLocalKeywordClient;
import com.meetup.server.global.clients.kakao.local.KakaoLocalRequest;
import com.meetup.server.global.clients.kakao.local.KakaoLocalResponse;
import com.meetup.server.global.clients.kakao.local.KakaoLocalResponse.KakaoSearchResponse;
import com.meetup.server.startpoint.exception.StartPointErrorType;
import com.meetup.server.startpoint.exception.StartPointException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class StartPointSearcher {

    private static final Set<String> SUPPORTED_REGIONS = Set.of("서울", "경기", "인천");

    private final KakaoLocalKeywordClient kakaoLocalKeywordClient;

    public KakaoLocalResponse search(String textQuery) {
        KakaoLocalResponse response = kakaoLocalKeywordClient.sendRequest(
                KakaoLocalRequest.builder()
                        .query(textQuery)
                        .build()
        );
        if (response == null) {
            throw new StartPointException(StartPointErrorType.PLACE_NOT_FOUND);
        }

        List<KakaoSearchResponse> filteredSeoulResponse = response.getKakaoSearchResponses().stream()
                .filter(this::isSupportedRegion)
                .toList();

        response.updateKakaoSearchResponse(filteredSeoulResponse);
        return response;
    }

    private boolean isSupportedRegion(KakaoSearchResponse response) {
        String address = response.getAddressName();
        if (address == null || address.isBlank()) {
            return false;
        }

        int spaceIdx = address.indexOf(' ');
        String firstWord = (spaceIdx == -1) ? address : address.substring(0, spaceIdx);

        return SUPPORTED_REGIONS.contains(firstWord);
    }

}
