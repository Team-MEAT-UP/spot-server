package com.meetup.server.user.domain;

import com.meetup.server.global.domain.BaseEntity;
import com.meetup.server.user.domain.type.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nickname", length = 30)
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "social_id", length = 100, nullable = false, unique = true)
    private String socialId;

    @Column(name = "email", length = 100, nullable = true, unique = true)
    private String email;

    @Column(name = "personal_info_agreement", nullable = false)
    private boolean personalInfoAgreement;

    @Column(name = "marketing_agreement")
    private boolean marketingAgreement;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10, nullable = false)
    private Role role;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(Long userId, String nickname, String profileImage, String email, String socialId, Role role, boolean personalInfoAgreement, boolean marketingAgreement, LocalDateTime deletedAt) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.email = email;
        this.socialId = socialId;
        this.role = role;
        this.personalInfoAgreement = personalInfoAgreement;
        this.marketingAgreement = marketingAgreement;
        this.deletedAt = deletedAt;
    }

    public void updateAgreement(boolean personalInfoAgreement, boolean marketingAgreement) {
        this.personalInfoAgreement = personalInfoAgreement;
        this.marketingAgreement = marketingAgreement;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void withdraw() {
        this.profileImage = null;
        this.personalInfoAgreement = false;
        this.marketingAgreement = false;
        this.nickname = null;
        this.role = Role.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
    }

    public void rejoin(User user) {
        this.nickname = user.nickname;
        this.profileImage = user.profileImage;
        this.email = user.email;
        this.socialId = user.socialId;
        this.role = Role.USER;
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
