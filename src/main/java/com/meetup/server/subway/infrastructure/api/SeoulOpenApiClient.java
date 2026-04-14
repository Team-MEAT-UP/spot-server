package com.meetup.server.subway.infrastructure.api;

import com.meetup.server.subway.infrastructure.api.dto.SeoulSubwayApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class SeoulOpenApiClient {

    private static final String API_URL_TEMPLATE = "/%s/json/CardSubwayStatsNew/%d/%d/%s";
    private static final int PAGE_SIZE = 1000;

    private final RestClient seoulSubwayRestClient;
    private final SeoulSubwayProperties seoulSubwayProperties;

    public SeoulOpenApiClient(
            RestClient seoulSubwayRestClient,
            SeoulSubwayProperties seoulSubwayProperties
    ) {
        this.seoulSubwayRestClient = seoulSubwayRestClient;
        this.seoulSubwayProperties = seoulSubwayProperties;
    }

    /**
     * 특정 날짜의 전체 지하철 승하차 데이터를 페이징하여 조회
     *
     * @param useYmd 조회 날짜 (yyyyMMdd)
     * @return 전체 Row 리스트
     */
    public List<SeoulSubwayApiResponse.Row> fetchAllByDate(String useYmd) {
        if (seoulSubwayProperties.key() == null || seoulSubwayProperties.key().isBlank()) {
            log.warn("[서울 Open API] API 키가 설정되지 않았습니다. seoul.open-api.key를 확인하세요.");
            return Collections.emptyList();
        }

        List<SeoulSubwayApiResponse.Row> allRows = new ArrayList<>();
        int startIndex = 1;

        while (true) {
            int endIndex = startIndex + PAGE_SIZE - 1;
            String url = String.format(API_URL_TEMPLATE, seoulSubwayProperties.key(), startIndex, endIndex, useYmd);

            try {
                SeoulSubwayApiResponse response = seoulSubwayRestClient.get()
                        .uri(url)
                        .retrieve()
                        .body(SeoulSubwayApiResponse.class);

                if (response == null || response.cardSubwayStatsNew() == null) {
                    log.warn("[서울 Open API] 응답이 null입니다. useYmd={}, startIndex={}", useYmd, startIndex);
                    break;
                }

                SeoulSubwayApiResponse.CardSubwayStatsNew data = response.cardSubwayStatsNew();

                if (!"INFO-000".equals(data.result().code())) {
                    log.warn("[서울 Open API] 에러 응답: code={}, message={}", data.result().code(), data.result().message());
                    break;
                }

                if (data.row() == null || data.row().isEmpty()) {
                    break;
                }

                allRows.addAll(data.row());

                if (allRows.size() >= data.listTotalCount()) {
                    break;
                }

                startIndex += PAGE_SIZE;
            } catch (Exception e) {
                log.error("[서울 Open API] 호출 실패: useYmd={}, startIndex={}", useYmd, startIndex, e);
                break;
            }
        }

        log.info("[서울 Open API] 조회 완료: useYmd={}, 총 {}건", useYmd, allRows.size());
        return allRows;
    }
}
