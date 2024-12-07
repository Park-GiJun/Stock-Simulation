package com.gijun.backend.domain.entity;

import com.gijun.backend.domain.dto.account.AccountStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private BigDecimal totalInvestment;

    @Column(nullable = false)
    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false)
    private LocalDateTime lastTradeAt;

    @Builder
    public Account(User user, String accountNumber, BigDecimal initialBalance) {
        this.user = user;
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.totalInvestment = BigDecimal.ZERO;
        this.totalValue = initialBalance;
        this.status = AccountStatus.ACTIVE;
        this.lastTradeAt = LocalDateTime.now();
    }
}