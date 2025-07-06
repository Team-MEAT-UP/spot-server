package com.meetup.server.subway.domain;

import com.meetup.server.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subway_connection")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class SubwayConnection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subway_connection_id")
    private Integer subwayConnectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_subway_id", nullable = false)
    private Subway fromSubway;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_subway_id", nullable = false)
    private Subway toSubway;

    @Column(name = "subway_line", nullable = false)
    private String line;

    @Column(name = "section_time_sec", nullable = false)
    private int sectionTimeSec;

    @Builder
    public SubwayConnection(Subway fromSubway, Subway toSubway, String line, int sectionTimeSec) {
        this.fromSubway = fromSubway;
        this.toSubway = toSubway;
        this.line = line;
        this.sectionTimeSec = sectionTimeSec;
    }
}
