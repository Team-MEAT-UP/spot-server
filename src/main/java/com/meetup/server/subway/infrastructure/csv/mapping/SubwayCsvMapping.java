package com.meetup.server.subway.infrastructure.csv.mapping;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;

@Getter
public class SubwayCsvMapping {

    @CsvBindByName(column = "호선")
    private String line;

    @CsvBindByName(column = "출발역 ID")
    private String fromCode;

    @CsvBindByName(column = "출발역")
    private String fromName;

    @CsvBindByName(column = "출발역 경도")
    private double fromLongitude;

    @CsvBindByName(column = "출발역 위도")
    private double fromLatitude;

    @CsvBindByName(column = "도착역 ID")
    private String toCode;

    @CsvBindByName(column = "도착역")
    private String toName;

    @CsvBindByName(column = "도착역 경도")
    private double toLongitude;

    @CsvBindByName(column = "도착역 위도")
    private double toLatitude;

    @CsvBindByName(column = "소요시간")
    private int sectionTime;
}
