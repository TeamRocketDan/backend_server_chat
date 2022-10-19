package com.example.teamrocket.user.entity;

import com.example.teamrocket.config.jpa.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_table")
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id; // PK

    private String username; // 이름
    private String email; // 이메일
    private String nickname; // 닉네임
    private String profileImage; // 프로필 이미지
    private String uuid; // 소셜 아이디
    private String password; // 비밀번호

    @Enumerated(EnumType.STRING)
    private ProviderType providerType; // 소셜 타입

    @Enumerated(EnumType.STRING)
    private RoleType roleType; // 권한

    private LocalDateTime deletedAt; // 삭제(탈퇴) 날짜

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateProfileImageUrl(String profileImage) {
        this.profileImage = profileImage;
    }
}