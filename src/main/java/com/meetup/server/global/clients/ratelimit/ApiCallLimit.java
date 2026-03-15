package com.meetup.server.global.clients.ratelimit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "api_call_limits", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"api_name", "call_date"})
})
public class ApiCallLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_name", nullable = false)
    private String apiName;

    @Column(name = "call_date", nullable = false)
    private LocalDate callDate;

    @Column(name = "call_count", nullable = false)
    private Integer count = 0;

    @Builder
    public ApiCallLimit(String apiName, LocalDate callDate, Integer count) {
        this.apiName = apiName;
        this.callDate = callDate;
        this.count = count;
    }

    public static ApiCallLimit create(String apiName, LocalDate callDate) {
        return ApiCallLimit.builder()
                .apiName(apiName)
                .callDate(callDate)
                .count(0)
                .build();
    }

    public void increment() {
        this.count++;
    }
}
