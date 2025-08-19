package com.meetup.server.subway.infrastructure.csv;

import com.meetup.server.global.support.DummyDataInit;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.startpoint.domain.type.Location;
import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.domain.SubwayConnection;
import com.meetup.server.subway.domain.TransferInfo;
import com.meetup.server.subway.infrastructure.csv.mapping.SubwayCsvMapping;
import com.meetup.server.subway.infrastructure.csv.mapping.TransferInfoMapping;
import com.meetup.server.subway.infrastructure.jpa.SubwayConnectionRepository;
import com.meetup.server.subway.infrastructure.jpa.SubwayRepository;
import com.meetup.server.subway.infrastructure.jpa.TransferInfoRepository;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Profile("local")
@Order(2)
@DummyDataInit
public class SubwayCsvLoader implements ApplicationRunner {

    private final SubwayRepository subwayRepository;
    private final SubwayConnectionRepository subwayConnectionRepository;
    private final TransferInfoRepository transferInfoRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (subwayRepository.count() == 0 || subwayConnectionRepository.count() == 0 || transferInfoRepository.count() == 0) {
            subwayConnectionRepository.deleteAll();
            transferInfoRepository.deleteAll();
            subwayRepository.deleteAll();

            subwayRepository.saveAll(parseSubwayCsv());
            subwayConnectionRepository.saveAll(parseSectionTimeCsv());
            transferInfoRepository.saveAll(parseTransferInfoCsv());
        }
    }

    private Set<Subway> parseSubwayCsv() throws IOException {
        ClassPathResource resource = new ClassPathResource("csv/SPOT_지하철역_정보.csv");

        try (Reader reader = Files.newBufferedReader(resource.getFile().toPath(), StandardCharsets.UTF_8)) {
            HeaderColumnNameMappingStrategy<SubwayCsvMapping> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(SubwayCsvMapping.class);

            CsvToBean<SubwayCsvMapping> csvToBean = new CsvToBeanBuilder<SubwayCsvMapping>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Set<Subway> subways = new HashSet<>();

            for (SubwayCsvMapping csvLine : csvToBean.parse()) {
                String line = csvLine.getLine();

                Subway fromSubway = Subway.builder()
                        .name(csvLine.getFromName())
                        .code(csvLine.getFromCode())
                        .line(line)
                        .location(Location.of(csvLine.getFromLongitude(), csvLine.getFromLatitude()))
                        .point(CoordinateUtil.createPoint(csvLine.getFromLongitude(), csvLine.getFromLatitude()))
                        .build();

                Subway toSubway = Subway.builder()
                        .name(csvLine.getToName())
                        .code(csvLine.getToCode())
                        .line(line)
                        .location(Location.of(csvLine.getToLongitude(), csvLine.getToLatitude()))
                        .point(CoordinateUtil.createPoint(csvLine.getToLongitude(), csvLine.getToLatitude()))
                        .build();

                subways.add(fromSubway);
                subways.add(toSubway);
            }

            return subways;
        }
    }

    private List<SubwayConnection> parseSectionTimeCsv() throws IOException {
        ClassPathResource resource = new ClassPathResource("csv/SPOT_지하철역_정보.csv");

        try (Reader reader = Files.newBufferedReader(resource.getFile().toPath(), StandardCharsets.UTF_8)) {
            HeaderColumnNameMappingStrategy<SubwayCsvMapping> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(SubwayCsvMapping.class);

            CsvToBean<SubwayCsvMapping> csvToBean = new CsvToBeanBuilder<SubwayCsvMapping>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<SubwayCsvMapping> records = csvToBean.parse();
            List<SubwayConnection> connections = new ArrayList<>();

            for (SubwayCsvMapping record : records) {
                Subway fromSubway = subwayRepository.findByCode(record.getFromCode()).orElse(null);
                Subway toSubway = subwayRepository.findByCode(record.getToCode()).orElse(null);

                if (fromSubway == null || toSubway == null) {
                    log.warn("지하철 정보 없음 - from: {} (code: {}) / to: {} (code: {})",
                            record.getFromName(), record.getFromCode(), record.getToName(), record.getToCode());
                    continue;
                }

                int sectionTimeSec = record.getSectionTime() * 60;

                SubwayConnection connectionAB = SubwayConnection.builder()
                        .fromSubway(fromSubway)
                        .toSubway(toSubway)
                        .line(record.getLine())
                        .sectionTimeSec(sectionTimeSec)
                        .build();
                connections.add(connectionAB);

                SubwayConnection connectionBA = SubwayConnection.builder()
                        .fromSubway(toSubway)
                        .toSubway(fromSubway)
                        .line(record.getLine())
                        .sectionTimeSec(sectionTimeSec)
                        .build();
                connections.add(connectionBA);
            }

            return connections;
        }
    }

    private List<TransferInfo> parseTransferInfoCsv() throws IOException {
        ClassPathResource resource = new ClassPathResource("csv/서울교통공사_서울 도시철도 환승정보_20250319.csv");

        try (Reader reader = Files.newBufferedReader(resource.getFile().toPath(), Charset.forName("EUC-KR"))) {
            HeaderColumnNameMappingStrategy<TransferInfoMapping> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(TransferInfoMapping.class);

            CsvToBean<TransferInfoMapping> csvToBean = new CsvToBeanBuilder<TransferInfoMapping>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<TransferInfoMapping> transferInfoMappings = csvToBean.parse();

            Set<String> existingTransferKeys = new HashSet<>();
            List<TransferInfo> transferInfos = new ArrayList<>();

            for (TransferInfoMapping mapping : transferInfoMappings) {
                Subway fromSubway = subwayRepository.findByCode(mapping.getFromCode()).orElse(null);
                Subway toSubway = subwayRepository.findByCode(mapping.getToCode()).orElse(null);

                if (fromSubway == null || toSubway == null) {
                    log.warn("지하철 정보 없음 - from: {} (code: {}, line: {}) / to: {} (code: {}, line: {})",
                            mapping.getFromName(), mapping.getFromCode(), mapping.getFromLine(),
                            mapping.getToName(), mapping.getToCode(), mapping.getToLine());
                    continue;
                }

                String key = fromSubway.getSubwayId() + "-" + toSubway.getSubwayId();
                if (existingTransferKeys.contains(key)) {
                    log.info("중복 환승 정보 무시됨 - from: {} / to: {}", fromSubway.getSubwayId(), toSubway.getSubwayId());
                    continue;
                }

                existingTransferKeys.add(key);

                TransferInfo transferInfo = TransferInfo.builder()
                        .fromSubway(fromSubway)
                        .toSubway(toSubway)
                        .build();

                transferInfos.add(transferInfo);
            }

            return transferInfos;
        }
    }
}
