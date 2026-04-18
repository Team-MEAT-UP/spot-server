package com.meetup.server.subway.implement.processor;

import com.meetup.server.subway.domain.Subway;
import com.meetup.server.subway.domain.SubwayConnection;
import com.meetup.server.subway.domain.TransferInfo;
import com.meetup.server.subway.infrastructure.jpa.SubwayConnectionRepository;
import com.meetup.server.subway.infrastructure.jpa.SubwayRepository;
import com.meetup.server.subway.infrastructure.jpa.TransferInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubwayPathProcessor {

    private static final int TRANSFER_TIME_SEC = 180;

    private final SubwayRepository subwayRepository;
    private final SubwayConnectionRepository subwayConnectionRepository;
    private final TransferInfoRepository transferInfoRepository;

    public SubwayPathResult findShortestPath(int startSubwayId, int endSubwayId) {

        Map<Integer, Subway> subwayMap = mapSubwaysById();
        Map<Integer, List<SubwayConnection>> connectionsMap = groupConnectionsByFromId();
        Map<Integer, List<TransferInfo>> transferMap = groupTransfersByFromId();

        PriorityQueue<SubwayPathNode> pathQueue = new PriorityQueue<>(
                Comparator.comparingInt((SubwayPathNode node) -> node.subwayIds().size())
                        .thenComparingInt(SubwayPathNode::totalTime)
        );
        pathQueue.offer(new SubwayPathNode(startSubwayId, 0, new ArrayList<>(List.of(startSubwayId))));

        Map<String, Integer> visitedTimeMap = new HashMap<>();

        while (!pathQueue.isEmpty()) {
            SubwayPathNode currentNode = pathQueue.poll();

            String pathKey = currentNode.subwayId() + "-" + currentNode.subwayIds().size();
            if (visitedTimeMap.containsKey(pathKey) && visitedTimeMap.get(pathKey) <= currentNode.totalTime()) {
                continue;
            }
            visitedTimeMap.put(pathKey, currentNode.totalTime());

            if (currentNode.subwayId() == endSubwayId) {
                List<String> stationNames = currentNode.subwayIds().stream()
                        .map(id -> subwayMap.get(id).getName())
                        .collect(Collectors.toList());

                return new SubwayPathResult(currentNode.totalTime(), currentNode.subwayIds(), stationNames);
            }

            List<SubwayConnection> connections = connectionsMap.getOrDefault(currentNode.subwayId(), List.of());
            for (SubwayConnection connection : connections) {
                pathQueue.offer(new SubwayPathNode(
                        connection.getToSubway().getSubwayId(),
                        currentNode.totalTime() + connection.getSectionTimeSec(),
                        addSubwayToPath(currentNode.subwayIds(), connection.getToSubway().getSubwayId())
                ));
            }

            List<TransferInfo> transfers = transferMap.getOrDefault(currentNode.subwayId(), List.of());
            for (TransferInfo transfer : transfers) {
                pathQueue.offer(new SubwayPathNode(
                        transfer.getToSubway().getSubwayId(),
                        currentNode.totalTime() + TRANSFER_TIME_SEC,
                        addSubwayToPath(currentNode.subwayIds(), transfer.getToSubway().getSubwayId())
                ));
            }
        }

        return null;
    }

    private List<Integer> addSubwayToPath(List<Integer> path, int subwayId) {
        List<Integer> newPath = new ArrayList<>(path);
        newPath.add(subwayId);
        return newPath;
    }

    private Map<Integer, Subway> mapSubwaysById() {
        return subwayRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Subway::getSubwayId, Function.identity()));
    }

    private Map<Integer, List<SubwayConnection>> groupConnectionsByFromId() {
        return subwayConnectionRepository.findAllWithSubways()
                .stream()
                .collect(Collectors.groupingBy(subwayConnection -> subwayConnection.getFromSubway().getSubwayId()));
    }

    private Map<Integer, List<TransferInfo>> groupTransfersByFromId() {
        return transferInfoRepository.findAllWithSubways()
                .stream()
                .collect(Collectors.groupingBy(transferInfo -> transferInfo.getFromSubway().getSubwayId()));
    }
}
