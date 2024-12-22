package com.gijun.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private LocalDateTime lastLogin;

    @Column(nullable = false, length = 1)
    @ColumnDefault("'Y'")
    private String useYn;

    @Column(nullable = false, length = 20)
    private String role; // 예: ROLE_USER, ROLE_ADMIN

    @Builder
    public User(String userId, String password, String username, String email, String role,
                LocalDateTime createdAt, LocalDateTime lastLoginAt) {
        this.userId = userId;
        this.password = password;
        this.username = username;
        this.role = role;
        this.email = email;
    }


    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void disable() {
        this.useYn = "N";
    }

    public void enable() {
        this.useYn = "Y";
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}