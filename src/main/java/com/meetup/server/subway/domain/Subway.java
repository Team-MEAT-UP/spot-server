package com.meetup.server.subway.domain;

import com.meetup.server.global.domain.BaseEntity;
import com.meetup.server.startpoint.domain.type.Location;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "subway")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "code", callSuper = false)
public class Subway extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subway_id")
    private Integer subwayId;

    @Column(name = "subway_name", length = 20, nullable = false)
    private String name;

    @Column(name = "subway_code", length = 5, nullable = false, unique = true)
    private String code;

    @Column(name = "subway_line", length = 10, nullable = false)
    private String line;

    @Embedded
    private Location location;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Point point;

    @Builder
    public Subway(String name, String code, String line, Location location, Point point) {
        this.name = name;
        this.code = code;
        this.line = line;
        this.location = location;
        this.point = point;
    }
}
