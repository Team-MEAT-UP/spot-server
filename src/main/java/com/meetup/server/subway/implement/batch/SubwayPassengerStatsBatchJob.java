package com.meetup.server.subway.implement.batch;

import com.meetup.server.global.util.TimeUtil;
import com.meetup.server.subway.domain.SubwayPassengerStats;
import com.meetup.server.subway.infrastructure.api.SeoulOpenApiClient;
import com.meetup.server.subway.infrastructure.api.dto.SeoulSubwayApiResponse;
import com.meetup.server.subway.infrastructure.jpa.SubwayPassengerStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubwayPassengerStatsBatchJob {

    private static final String EVERY_3AM = "0 0 3 * * *";
    private static final int DATA_DELAY_DAYS = 5;
    private static final int DATA_RETENTION_DAYS = 14;

    private final SeoulOpenApiClient seoulOpenApiClient;
    private final SubwayPassengerStatsRepository statsRepository;

    /**
     * 매일 새벽 3시에 실행
     * 서울시 API는 3일 전 데이터를 갱신하므로, 5일 전 날짜 데이터를 수집
     */
    @Scheduled(cron = EVERY_3AM, zone = "Asia/Seoul")
    @Transactional
    public void collectDailyStats() {
        LocalDate targetDate = LocalDate.now().minusDays(DATA_DELAY_DAYS);

        if (statsRepository.existsByUseDate(targetDate)) {
            log.info("[승하차 배치] 이미 수집된 날짜입니다: {}", targetDate);
            return;
        }

        String useYmd = TimeUtil.formatAsDate(targetDate);
        log.info("[승하차 배치] 수집 시작: {}", useYmd);

        List<SeoulSubwayApiResponse.Row> rows = seoulOpenApiClient.fetchAllByDate(useYmd);

        if (rows.isEmpty()) {
            log.warn("[승하차 배치] 수집된 데이터가 없습니다: {}", useYmd);
            return;
        }

        List<SubwayPassengerStats> stats = rows.stream()
                .map(row -> SubwayPassengerStats.builder()
                        .stationName(row.stationName())
                        .lineName(row.lineName())
                        .boardingCount(row.boardingCount())
                        .alightingCount(row.alightingCount())
                        .useDate(targetDate)
                        .build())
                .toList();

        statsRepository.saveAll(stats);
        log.info("[승하차 배치] 저장 완료: {}건", stats.size());

        cleanupOldData();
    }

    /**
     * 보관 기간(14일) 이전 데이터 삭제
     */
    private void cleanupOldData() {
        LocalDate cutoffDate = LocalDate.now().minusDays(DATA_RETENTION_DAYS);
        statsRepository.deleteByUseDateBefore(cutoffDate);
        log.info("[승하차 배치] {} 이전 데이터 정리 완료", cutoffDate);
    }
}
