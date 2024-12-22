package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.Account;
import com.gijun.backend.domain.entity.Stock;
import com.gijun.backend.domain.entity.UserStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStockRepository extends JpaRepository<UserStock, Long> {

    // 특정 종목의 전체 보유 계정 조회
    List<UserStock> findByStock(Stock stock);

    // 계정의 총 평가금액 조회
    @Query("SELECT SUM(us.currentValue) FROM UserStock us WHERE us.account = :account")
    BigDecimal calculateTotalValue(@Param("account") Account account);

    // 계정의 총 평가손익 조회
    @Query("SELECT SUM(us.totalGain) FROM UserStock us WHERE us.account = :account")
    BigDecimal calculateTotalGain(@Param("account") Account account);

    // 특정 수익률 이상인 보유 주식 조회
    @Query("SELECT us FROM UserStock us WHERE us.account = :account AND us.gainRate >= :rate")
    List<UserStock> findProfitableStocks(
            @Param("account") Account account,
            @Param("rate") Double rate
    );

    // 특정 종목의 총 보유수량 조회
    @Query("SELECT SUM(us.quantity) FROM UserStock us WHERE us.stock = :stock")
    Integer getTotalHoldingQuantity(@Param("stock") Stock stock);

    Optional<UserStock> findByAccountAndStock(Account account, Stock stock);
    List<UserStock> findByAccount(Account account);

    // 보유 종목 목록 페이징 (기본)
    Page<UserStock> findByAccount(Account account, Pageable pageable);

    // 수익률 기준 정렬 + 페이징
    Page<UserStock> findByAccountOrderByGainRateDesc(Account account, Pageable pageable);

    // 평가금액 기준 정렬 + 페이징
    Page<UserStock> findByAccountOrderByCurrentValueDesc(Account account, Pageable pageable);

    // 수량 기준 정렬 + 페이징
    Page<UserStock> findByAccountOrderByQuantityDesc(Account account, Pageable pageable);

    // 특정 수익률 이상인 종목 조회
    @Query("SELECT us FROM UserStock us WHERE us.account = :account AND us.gainRate >= :rate ORDER BY us.gainRate DESC")
    Page<UserStock> findProfitableStocks(@Param("account") Account account, @Param("rate") Double rate, Pageable pageable);
}