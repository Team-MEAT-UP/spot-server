package com.meetup.server.subway.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SeoulSubwayApiResponse(
        @JsonProperty("CardSubwayStatsNew") CardSubwayStatsNew cardSubwayStatsNew
) {

    public record CardSubwayStatsNew(
            @JsonProperty("list_total_count") int listTotalCount,
            @JsonProperty("RESULT") Result result,
            @JsonProperty("row") List<Row> row
    ) {
    }

    public record Result(
            @JsonProperty("CODE") String code,
            @JsonProperty("MESSAGE") String message
    ) {
    }

    public record Row(
            @JsonProperty("USE_YMD") String useYmd,
            @JsonProperty("SBWY_ROUT_LN_NM") String lineName,
            @JsonProperty("SBWY_STNS_NM") String stationName,
            @JsonProperty("GTON_TNOPE") long boardingCount,
            @JsonProperty("GTOFF_TNOPE") long alightingCount
    ) {
    }
}
