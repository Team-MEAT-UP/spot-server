package com.meetup.server.log.domain;

import com.meetup.server.global.domain.BaseEntity;
import com.meetup.server.global.util.StringUtil;
import com.meetup.server.log.domain.type.InflowType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "log_event_inflow")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LogEventInflow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_event_inflow_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "inflow_type", nullable = false)
    private InflowType inflowType;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    public static LogEventInflow create(InflowType inflowType, UUID eventId, Long userId, String ipAddress, String userAgent) {
        LogEventInflow logEventInflow = new LogEventInflow();
        logEventInflow.inflowType = inflowType;
        logEventInflow.eventId = eventId;
        logEventInflow.userId = userId;
        logEventInflow.ipAddress = ipAddress;
        logEventInflow.userAgent = StringUtil.truncate(userAgent, 255);
        return logEventInflow;
    }
}
