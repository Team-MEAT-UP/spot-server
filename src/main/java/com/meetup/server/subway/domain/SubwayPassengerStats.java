package com.meetup.server.subway.domain;

import com.meetup.server.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "subway_passenger_stats", uniqueConstraints = @UniqueConstraint(columnNames = {"station_name", "line_name", "use_date"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SubwayPassengerStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stats_id")
    private Long id;

    @Column(name = "station_name", length = 30, nullable = false)
    private String stationName;

    @Column(name = "line_name", length = 20, nullable = false)
    private String lineName;

    @Column(name = "boarding_count", nullable = false)
    private Long boardingCount;

    @Column(name = "alighting_count", nullable = false)
    private Long alightingCount;

    @Column(name = "total_count", nullable = false)
    private Long totalCount;

    @Column(name = "use_date", nullable = false)
    private LocalDate useDate;

    @Builder
    public SubwayPassengerStats(String stationName, String lineName, Long boardingCount, Long alightingCount, LocalDate useDate) {
        this.stationName = stationName;
        this.lineName = lineName;
        this.boardingCount = boardingCount;
        this.alightingCount = alightingCount;
        this.totalCount = boardingCount + alightingCount;
        this.useDate = useDate;
    }
}
