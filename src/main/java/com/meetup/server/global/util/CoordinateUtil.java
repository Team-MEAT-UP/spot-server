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

    public static Point calculateCenterPoint(List<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("중간 좌표를 계산할 좌표 리스트가 존재하지 않습니다.");
        }

        if (points.size() == 1) {
            return points.getFirst();
        }

        // Weiszfeld 알고리즘을 사용
        double medianLongitude = points.stream().mapToDouble(Point::getX).average().orElse(0);
        double medianLatitude = points.stream().mapToDouble(Point::getY).average().orElse(0);

        final int MAX_ITERATIONS = 100;
        final double CONVERGENCE_THRESHOLD = 1e-7;

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            double weightSum = 0;
            double weightedLongitude = 0;
            double weightedLatitude = 0;

            for (Point point : points) {
                double dx = point.getX() - medianLongitude;
                double dy = point.getY() - medianLatitude;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < CONVERGENCE_THRESHOLD) {
                    continue;
                }

                double weight = 1.0 / distance;
                weightSum += weight;
                weightedLongitude += weight * point.getX();
                weightedLatitude += weight * point.getY();
            }

            if (weightSum == 0) {
                break;
            }

            double newLongitude = weightedLongitude / weightSum;
            double newLatitude = weightedLatitude / weightSum;

            double shift = Math.sqrt(
                    Math.pow(newLongitude - medianLongitude, 2) + Math.pow(newLatitude - medianLatitude, 2)
            );

            medianLongitude = newLongitude;
            medianLatitude = newLatitude;

            if (shift < CONVERGENCE_THRESHOLD) {
                break;
            }
        }

        return createPoint(medianLongitude, medianLatitude);
    }
}
