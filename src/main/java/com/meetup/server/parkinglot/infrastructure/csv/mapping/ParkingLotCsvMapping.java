package com.meetup.server.parkinglot.infrastructure.csv.mapping;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;

@Getter
public class ParkingLotCsvMapping {

    @CsvBindByName(column = "주차장명")
    private String name;

    @CsvBindByName(column = "경도")
    private Double longitude;

    @CsvBindByName(column = "위도")
    private Double latitude;
}
