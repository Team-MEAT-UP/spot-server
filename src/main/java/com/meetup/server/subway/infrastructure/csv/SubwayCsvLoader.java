package com.meetup.server.subway.infrastructure.csv;

import com.meetup.server.global.support.DummyDataInit;
import com.meetup.server.global.util.CoordinateUtil;
import com.meetup.server.startpoint.domain.type.Location;
import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.domain.SubwayConnection;
import com.meetup.server.subway.domain.TransferInfo;
import com.meetup.server.subway.infrastructure.csv.mapping.SubwayCsvMapping;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

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
        long subwayCount = subwayRepository.count();
        long connCount = subwayConnectionRepository.count();
        long transferCount = transferInfoRepository.count();

        if (subwayCount == 0 && connCount == 0 && transferCount == 0) {
            log.info("지하철 데이터가 모두 비어있어 초기화를 시작합니다.");

            Map<String, Subway> subwayCodeMap = loadAndSaveSubways();

            List<SubwayConnection> connections = parseSectionTimeCsv(subwayCodeMap);
            subwayConnectionRepository.saveAll(connections);

            List<TransferInfo> transferInfos = generateTransferInfo(subwayCodeMap.values());
            transferInfoRepository.saveAll(transferInfos);

            log.info("지하철 데이터 적재 완료 - 역: {}, 구간: {}, 환승: {}",
                    subwayCodeMap.size(), connections.size(), transferInfos.size());
        }
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) return null;
        try {
            return String.valueOf(Integer.parseInt(code.trim()));
        } catch (NumberFormatException e) {
            return code.trim();
        }
    }

    private Map<String, Subway> loadAndSaveSubways() throws IOException {
        ClassPathResource resource = new ClassPathResource("csv/SPOT_지하철역_정보.csv");

        try (Reader reader = Files.newBufferedReader(resource.getFile().toPath(), StandardCharsets.UTF_8)) {
            HeaderColumnNameMappingStrategy<SubwayCsvMapping> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(SubwayCsvMapping.class);

            CsvToBean<SubwayCsvMapping> csvToBean = new CsvToBeanBuilder<SubwayCsvMapping>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Map<String, Subway> subwayCodeMap = new HashMap<>();
            for (SubwayCsvMapping csvLine : csvToBean.parse()) {
                String line = csvLine.getLine();

                String fromCode = normalizeCode(csvLine.getFromCode());
                if (!subwayCodeMap.containsKey(fromCode)) {
                    Subway fromSubway = Subway.builder()
                            .name(csvLine.getFromName())
                            .code(fromCode)
                            .line(line)
                            .location(Location.of(csvLine.getFromLongitude(), csvLine.getFromLatitude()))
                            .point(CoordinateUtil.createPoint(csvLine.getFromLongitude(), csvLine.getFromLatitude()))
                            .build();
                    subwayCodeMap.put(fromCode, fromSubway);
                }

                String toCode = normalizeCode(csvLine.getToCode());
                if (!subwayCodeMap.containsKey(toCode)) {
                    Subway toSubway = Subway.builder()
                            .name(csvLine.getToName())
                            .code(toCode)
                            .line(line)
                            .location(Location.of(csvLine.getToLongitude(), csvLine.getToLatitude()))
                            .point(CoordinateUtil.createPoint(csvLine.getToLongitude(), csvLine.getToLatitude()))
                            .build();
                    subwayCodeMap.put(toCode, toSubway);
                }
            }

            List<Subway> savedSubways = subwayRepository.saveAll(subwayCodeMap.values());

            Map<String, Subway> savedSubwayMap = new HashMap<>();
            for (Subway s : savedSubways) {
                savedSubwayMap.put(s.getCode(), s);
            }
            return savedSubwayMap;
        }
    }

    private List<SubwayConnection> parseSectionTimeCsv(Map<String, Subway> subwayCodeMap) throws IOException {
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
                Subway fromSubway = subwayCodeMap.get(normalizeCode(record.getFromCode()));
                Subway toSubway = subwayCodeMap.get(normalizeCode(record.getToCode()));

                if (fromSubway == null || toSubway == null) {
                    log.warn("지하철 구간 연결 누락 - from: {} (code: {}) / to: {} (code: {})",
                            record.getFromName(), record.getFromCode(), record.getToName(), record.getToCode());
                    continue;
                }

                int sectionTimeSec = record.getSectionTime() * 60;

                connections.add(SubwayConnection.builder()
                        .fromSubway(fromSubway)
                        .toSubway(toSubway)
                        .line(record.getLine())
                        .sectionTimeSec(sectionTimeSec)
                        .build());

                connections.add(SubwayConnection.builder()
                        .fromSubway(toSubway)
                        .toSubway(fromSubway)
                        .line(record.getLine())
                        .sectionTimeSec(sectionTimeSec)
                        .build());
            }

            return connections;
        }
    }

    private List<TransferInfo> generateTransferInfo(Collection<Subway> allSubways) {
        Map<String, List<Subway>> subwaysByName = allSubways.stream()
                .collect(Collectors.groupingBy(Subway::getName));

        Set<String> existingKeys = new HashSet<>();
        List<TransferInfo> transferInfos = new ArrayList<>();

        for (List<Subway> sameNameSubways : subwaysByName.values()) {
            if (sameNameSubways.size() < 2) continue;

            for (int i = 0; i < sameNameSubways.size(); i++) {
                for (int j = i + 1; j < sameNameSubways.size(); j++) {
                    Subway from = sameNameSubways.get(i);
                    Subway to = sameNameSubways.get(j);

                    String keyAB = from.getSubwayId() + "-" + to.getSubwayId();
                    String keyBA = to.getSubwayId() + "-" + from.getSubwayId();

                    if (!existingKeys.contains(keyAB)) {
                        existingKeys.add(keyAB);
                        transferInfos.add(TransferInfo.builder()
                                .fromSubway(from).toSubway(to).build());
                    }
                    if (!existingKeys.contains(keyBA)) {
                        existingKeys.add(keyBA);
                        transferInfos.add(TransferInfo.builder()
                                .fromSubway(to).toSubway(from).build());
                    }
                }
            }
        }

        return transferInfos;
    }
}
