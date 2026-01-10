package com.meetup.server.log.domain;

import com.meetup.server.global.domain.BaseEntity;
import com.meetup.server.global.util.StringUtil;
import com.meetup.server.log.domain.type.LoginStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "log_user_login")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LogUserLogin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_user_login_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_status", nullable = false)
    private LoginStatus loginStatus;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "fail_reason")
    private String failReason;

    @Builder
    public LogUserLogin(Long userId, LoginStatus loginStatus, String ipAddress, String userAgent, String failReason) {
        this.userId = userId;
        this.loginStatus = loginStatus;
        this.ipAddress = ipAddress;
        this.userAgent = StringUtil.truncate(userAgent, 255);
        this.failReason = StringUtil.truncate(failReason, 255);
    }
}
