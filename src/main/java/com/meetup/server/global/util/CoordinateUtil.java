package com.meetup.server.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoordinateUtil {

    public static Point createPoint(double longitude, double latitude) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Coordinate coordinate = new Coordinate(longitude, latitude);
        return geometryFactory.createPoint(coordinate);
    }

    public static double calculateDistanceInMeters(Point p1, Point p2) {
        double lat1 = Math.toRadians(p1.getY());
        double lat2 = Math.toRadians(p2.getY());
        double dLat = Math.toRadians(p2.getY() - p1.getY());
        double dLon = Math.toRadians(p2.getX() - p1.getX());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371000 * c;
    }

    public static Point calculateCenterPoint(List<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("중간 좌표를 계산할 좌표 리스트가 존재하지 않습니다.");
        }

        if (points.size() == 1) {
            return points.getFirst();
        }

        double centerLongitude = points.stream().mapToDouble(Point::getX).average().orElse(0);
        double centerLatitude = points.stream().mapToDouble(Point::getY).average().orElse(0);

        return createPoint(centerLongitude, centerLatitude);
    }
}
