package com.meetup.server.subway.infrastructure.jpa;

import com.meetup.server.subway.domain.SubwayConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubwayConnectionRepository extends JpaRepository<SubwayConnection, Integer> {

    @Query("SELECT sc FROM SubwayConnection sc JOIN FETCH sc.fromSubway JOIN FETCH sc.toSubway")
    List<SubwayConnection> findAllWithSubways();
}
