package com.gijun.backend.domain.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class AccountResponseDTO {
    private Long accountId;         // 계좌 ID
    private String accountNumber;   // 계좌 번호
    private BigDecimal balance;     // 초기 잔고
}