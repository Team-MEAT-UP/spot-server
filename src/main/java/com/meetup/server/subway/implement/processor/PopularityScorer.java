package com.meetup.server.subway.implement.processor;

import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.infrastructure.jpa.SubwayPassengerStatsRepository;
import com.meetup.server.subway.infrastructure.jpa.projection.SubwayAveragePassenger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularityScorer {

    private static final double POPULARITY_WEIGHT = 0.7;
    private static final double PROXIMITY_WEIGHT = 0.3;
    private static final int STATS_DAYS = 14;
    private static final int MAX_POPULAR_COUNT = 3;

    private final SubwayPassengerStatsRepository statsRepository;

    /**
     * 후보 역 리스트에서 인기별(승하차 인원 + 좌표 근접도) 상위 역들을 선정
     *
     * @param candidateSubways 후보 역 리스트 (centerPoint 주변 역들)
     * @param centerPoint      중간 지점 좌표
     * @return 인기 점수 상위 3개 역
     */
    public List<Subway> scoreAndRank(List<Subway> candidateSubways, Point centerPoint) {
        if (candidateSubways == null || candidateSubways.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 후보 역들의 역명 리스트 추출
        List<String> stationNames = candidateSubways.stream()
                .map(SubwayNameMapper::toApiStationName)
                .distinct()
                .toList();

        // 2. DB에서 최근 평균 승하차 조회
        LocalDate fromDate = LocalDate.now().minusDays(STATS_DAYS);
        Map<String, Double> averagePassengers = fetchAveragePassengers(stationNames, fromDate);

        if (averagePassengers.isEmpty()) {
            log.warn("[인기별 스코어링] 승하차 데이터가 없습니다. 좌표 근접도만으로 정렬합니다.");
            return candidateSubways.stream()
                    .sorted(Comparator.comparingDouble(s -> CoordinateUtil.calculateDistanceInMeters(s.getPoint(), centerPoint)))
                    .limit(MAX_POPULAR_COUNT)
                    .toList();
        }

        // 3. 거리 계산
        Map<Integer, Double> distances = candidateSubways.stream()
                .collect(Collectors.toMap(
                        Subway::getSubwayId,
                        s -> CoordinateUtil.calculateDistanceInMeters(s.getPoint(), centerPoint)
                ));

        // 4. 복합 점수 계산 및 정렬
        double maxPassenger = averagePassengers.values().stream().mapToDouble(d -> d).max().orElse(1);
        double minPassenger = averagePassengers.values().stream().mapToDouble(d -> d).min().orElse(0);
        double passengerRange = Math.max(maxPassenger - minPassenger, 1.0);

        double maxDistance = distances.values().stream().mapToDouble(d -> d).max().orElse(1);
        double minDistance = distances.values().stream().mapToDouble(d -> d).min().orElse(0);
        double distanceRange = Math.max(maxDistance - minDistance, 1.0);

        List<ScoredSubway> ranked = candidateSubways.stream()
                .map(subway -> {
                    String stationName = SubwayNameMapper.toApiStationName(subway);
                    double avgPassenger = averagePassengers.getOrDefault(stationName, 0.0);
                    double distance = distances.getOrDefault(subway.getSubwayId(), maxDistance);

                    double normalizedPassenger = (avgPassenger - minPassenger) / passengerRange;
                    double normalizedDistance = (distance - minDistance) / distanceRange;

                    double score = POPULARITY_WEIGHT * normalizedPassenger + PROXIMITY_WEIGHT * (1 - normalizedDistance);

                    return new ScoredSubway(subway, score);
                })
                .sorted(Comparator.comparingDouble(ScoredSubway::score).reversed())
                .toList();

        // 5. 동일 역명 중복 제거 후 상위 3개 반환
        Set<String> selectedNames = new HashSet<>();
        return ranked.stream()
                .filter(scored -> selectedNames.add(scored.subway().getName()))
                .limit(MAX_POPULAR_COUNT)
                .map(ScoredSubway::subway)
                .toList();
    }

    private Map<String, Double> fetchAveragePassengers(List<String> stationNames, LocalDate fromDate) {
        List<SubwayAveragePassenger> averagePassengers = statsRepository.findAverageTotalByStationNames(stationNames, fromDate);
        return averagePassengers.stream()
                .collect(Collectors.toMap(
                        SubwayAveragePassenger::getStationName,
                        SubwayAveragePassenger::getAverageCount,
                        (existing, replacement) -> existing
                ));
    }

    private record ScoredSubway(Subway subway, double score) {
    }
}
