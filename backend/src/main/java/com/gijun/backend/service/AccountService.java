package com.gijun.backend.service;

import com.gijun.backend.domain.dto.account.AccountStatus;
import com.gijun.backend.domain.entity.Account;
import com.gijun.backend.domain.entity.User;
import com.gijun.backend.exception.BusinessException;
import com.gijun.backend.exception.ErrorCode;
import com.gijun.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    public Account getAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Transactional
    public Account createAccount(User user) {
        // 기존 계좌 확인
        if (accountRepository.existsByUserId(user.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }

        // 계좌번호 생성 (예: 'A' + UUID 앞 8자리)
        String accountNumber = generateAccountNumber();

        // 계좌 생성 (초기 잔고 0원)
        Account account = Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .initialBalance(BigDecimal.valueOf(10000000))
                .build();

        return accountRepository.save(account);
    }

    @Transactional
    public void updateBalance(Account account, BigDecimal amount, boolean isDecrease) {
        // 계좌 상태 확인
        validateAccountStatus(account);

        BigDecimal newBalance;
        if (isDecrease) {
            // 감소 시 잔고 확인
            if (account.getBalance().compareTo(amount) < 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
            }
            newBalance = account.getBalance().subtract(amount);
        } else {
            newBalance = account.getBalance().add(amount);
        }

        account.updateBalance(newBalance);
        accountRepository.save(account);

        log.info("Balance updated - Account: {}, Amount: {}, IsDecrease: {}, NewBalance: {}",
                account.getAccountNumber(), amount, isDecrease, newBalance);
    }

    @Transactional
    public void suspendAccount(Account account, String reason) {
        validateAccountStatus(account);
        account.suspend(reason);
        accountRepository.save(account);

        log.info("Account suspended - Account: {}, Reason: {}",
                account.getAccountNumber(), reason);
    }

    @Transactional
    public void activateAccount(Account account) {
        if (account.getStatus() != AccountStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }

        account.activate();
        accountRepository.save(account);

        log.info("Account activated - Account: {}", account.getAccountNumber());
    }

    private void validateAccountStatus(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
    }

    private String generateAccountNumber() {
        return "A" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

