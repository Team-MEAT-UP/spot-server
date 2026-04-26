package com.meetup.server.event.dto.response.route;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.meetup.server.event.domain.type.TrafficType;
import com.meetup.server.global.clients.odsay.OdsayTransitRouteSearchResponse;
import com.meetup.server.subway.dto.response.PassStopList;
import com.meetup.server.subway.dto.response.Stations;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record TransitRouteResponse(
        TrafficType trafficType,    //1: 지하철, 2: 버스, 3: 도보
        String startExitNo,
        String endExitNo,
        double distance,
        String laneName,  //지하철 노선명
        String startBoardName,
        String endBoardName,
        int stationCount,
        PassStopList passStopList,
        int sectionTime //이동 소요 시간
) {
    public static List<TransitRouteResponse> from(OdsayTransitRouteSearchResponse response) {
        return Optional.ofNullable(response)
                .map(OdsayTransitRouteSearchResponse::data)
                .map(data -> Optional.ofNullable(data.path()).orElse(List.of()))
                .filter(paths -> !paths.isEmpty())
                .map(List::getFirst)
                .map(firstPath -> Optional.ofNullable(firstPath.subPath()).orElse(List.of()).stream()
                        .map(subPath -> {
                            String laneName = Optional.ofNullable(subPath.lane())
                                    .flatMap(lanes -> lanes.stream().findFirst())
                                    .map(lane -> switch (subPath.trafficType()) {
                                        case 1 -> lane.name();   // 지하철
                                        case 2 -> lane.busNo();  // 버스
                                        case 3 -> lane.name();   // 도보
                                        default -> null;
                                    })
                                    .orElse(null);

                            List<Stations> passStopList = convertToDomainStations(
                                    Optional.ofNullable(subPath.passStopList())
                                            .map(OdsayTransitRouteSearchResponse.TransitData.PassStopList::stations)
                                            .orElse(List.of()));

                            return TransitRouteResponse.builder()
                                    .trafficType(TrafficType.fromCode(subPath.trafficType()))
                                    .distance(subPath.distance())
                                    .laneName(laneName)
                                    .startBoardName(subPath.startName())
                                    .endBoardName(subPath.endName())
                                    .stationCount(Optional.ofNullable(subPath.stationCount()).orElse(0))
                                    .passStopList(subPath.trafficType() == 3 ? null : new PassStopList(passStopList))
                                    .startExitNo(subPath.startExitNo())
                                    .endExitNo(subPath.endExitNo())
                                    .sectionTime(subPath.sectionTime())
                                    .build();
                        })
                        .toList()
                ).orElse(null);
    }

    private static List<Stations> convertToDomainStations(List<OdsayTransitRouteSearchResponse.TransitData.Station> stations) {
        if (stations == null || stations.isEmpty()) {
            return List.of();
        }
        return stations.stream()
                .map(station -> new Stations(
                        station.index(),
                        station.stationName(),
                        station.x(),
                        station.y()
                ))
                .toList();
    }
}
