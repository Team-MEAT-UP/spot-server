package com.meetup.server.global.clients.ratelimit;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ApiCallLimitRepository extends JpaRepository<ApiCallLimit, Long> {

    @Modifying
    @Query(value = "INSERT INTO api_call_limits (api_name, call_date, call_count) VALUES (:apiName, :callDate, 0) ON CONFLICT (api_name, call_date) DO NOTHING", nativeQuery = true)
    void initApiCallLimit(@Param("apiName") String apiName, @Param("callDate") LocalDate callDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ApiCallLimit> findWithLockByApiNameAndCallDate(String apiName, LocalDate callDate);

    Optional<ApiCallLimit> findByApiNameAndCallDate(String apiName, LocalDate callDate);
}
