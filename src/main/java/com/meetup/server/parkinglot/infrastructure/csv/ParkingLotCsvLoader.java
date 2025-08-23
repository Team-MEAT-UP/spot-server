package com.meetup.server.parkinglot.infrastructure.csv;

import com.meetup.server.global.support.DummyDataInit;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.parkinglot.domain.ParkingLot;
import com.meetup.server.parkinglot.infrastructure.csv.mapping.ParkingLotCsvMapping;
import com.meetup.server.parkinglot.infrastructure.jpa.ParkingLotRepository;
import com.meetup.server.startpoint.domain.type.Location;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Profile("local")
@Order(3)
@DummyDataInit
public class ParkingLotCsvLoader implements ApplicationRunner {

    private final ParkingLotRepository parkingLotRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (parkingLotRepository.count() == 0) {
            parkingLotRepository.saveAll(parseParkingLotCsv());
        }
    }

    private List<ParkingLot> parseParkingLotCsv() throws IOException {
        ClassPathResource resource = new ClassPathResource("csv/서울시 공영주차장 안내 정보.csv");

        try (Reader reader = Files.newBufferedReader(resource.getFile().toPath(), Charset.forName("EUC-KR"))) {
            HeaderColumnNameMappingStrategy<ParkingLotCsvMapping> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(ParkingLotCsvMapping.class);

            CsvToBean<ParkingLotCsvMapping> csvToBean = new CsvToBeanBuilder<ParkingLotCsvMapping>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse()
                    .stream()
                    .filter(csvLine -> csvLine.getLongitude() != null && csvLine.getLatitude() != null)
                    .map(csvLine -> ParkingLot.builder()
                            .name(csvLine.getName())
                            .location(Location.of(csvLine.getLongitude(), csvLine.getLatitude()))
                            .point(CoordinateUtil.createPoint(csvLine.getLongitude(), csvLine.getLatitude()))
                            .build()
                    ).toList();
        }
    }
}
