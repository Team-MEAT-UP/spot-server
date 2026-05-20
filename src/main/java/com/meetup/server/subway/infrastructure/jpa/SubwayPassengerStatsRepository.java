package com.meetup.server.subway.infrastructure.jpa;

import com.meetup.server.subway.domain.SubwayPassengerStats;
import com.meetup.server.subway.infrastructure.jpa.projection.SubwayAveragePassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SubwayPassengerStatsRepository extends JpaRepository<SubwayPassengerStats, Long> {

    @Query("""
            SELECT s.stationName as stationName, AVG(s.totalCount) as averageCount
            FROM SubwayPassengerStats s
            WHERE s.stationName IN :stationNames
              AND s.useDate >= :fromDate
            GROUP BY s.stationName
    """)
    List<SubwayAveragePassenger> findAverageTotalByStationNames(
            @Param("stationNames") List<String> stationNames,
            @Param("fromDate") LocalDate fromDate
    );

    boolean existsByUseDate(LocalDate useDate);

    @Modifying
    @Query("DELETE FROM SubwayPassengerStats s WHERE s.useDate < :cutoffDate")
    void deleteByUseDateBefore(@Param("cutoffDate") LocalDate cutoffDate);
}
