package com.gijun.backend.repository;

import com.gijun.backend.domain.dto.order.OrderStatus;
import com.gijun.backend.domain.entity.Account;
import com.gijun.backend.domain.entity.Order;
import com.gijun.backend.domain.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 계정별 주문 내역 조회
    Page<Order> findByAccountOrderByCreatedAtDesc(Account account, Pageable pageable);

    // 종목별 주문 내역 조회
    Page<Order> findByStockOrderByCreatedAtDesc(Stock stock, Pageable pageable);

    // 계정의 특정 종목 주문 내역 조회
    Page<Order> findByAccountAndStockOrderByCreatedAtDesc(Account account, Stock stock, Pageable pageable);

    // 상태별 주문 내역 조회
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    // 특정 기간 내 주문 내역 조회
    Page<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 계정별 특정 기간 주문 내역 조회
    @Query("SELECT o FROM Order o WHERE o.account = :account AND o.createdAt BETWEEN :start AND :end ORDER BY o.createdAt DESC")
    Page<Order> findAccountOrdersInPeriod(
            @Param("account") Account account,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // 미체결 주문 조회
    List<Order> findByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
            OrderStatus status,
            LocalDateTime before
    );

    // 계정별 미체결 주문 조회
    List<Order> findByAccountAndStatusOrderByCreatedAtAsc(
            Account account,
            OrderStatus status
    );


    // 계정 + 상태별 주문 내역 조회
    Page<Order> findByAccountAndStatusOrderByCreatedAtDesc(Account account, OrderStatus status, Pageable pageable);
}
