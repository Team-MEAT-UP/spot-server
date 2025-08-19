package com.meetup.server.subway.infrastructure.jpa;

import com.meetup.server.subway.domain.TransferInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransferInfoRepository extends JpaRepository<TransferInfo, Integer> {

    @Query("SELECT t FROM TransferInfo t JOIN FETCH t.fromSubway JOIN FETCH t.toSubway")
    List<TransferInfo> findAllWithSubways();
}
