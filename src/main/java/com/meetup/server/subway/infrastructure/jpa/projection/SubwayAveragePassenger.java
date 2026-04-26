package com.meetup.server.subway.infrastructure.jpa.projection;

public interface SubwayAveragePassenger {
    String getStationName();
    Double getAverageCount();
}
