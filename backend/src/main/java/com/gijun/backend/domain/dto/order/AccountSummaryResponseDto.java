package com.gijun.backend.domain.dto.order;

import com.gijun.backend.domain.dto.account.AccountStatus;
import com.gijun.backend.domain.entity.Account;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class AccountSummaryResponseDto {
    private final String accountNumber;
    private final BigDecimal balance;
    private final BigDecimal totalInvestment;
    private final BigDecimal totalValue;
    private final AccountStatus status;
    private final LocalDateTime lastTradeAt;

    public AccountSummaryResponseDto(Account account) {
        this.accountNumber = account.getAccountNumber();
        this.balance = account.getBalance();
        this.totalInvestment = account.getTotalInvestment();
        this.totalValue = account.getTotalValue();
        this.status = account.getStatus();
        this.lastTradeAt = account.getLastTradeAt();
    }
}