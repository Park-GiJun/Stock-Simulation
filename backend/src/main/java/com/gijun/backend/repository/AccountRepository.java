package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    Optional<Account> findByAccountNumber(String accountNumber);
}