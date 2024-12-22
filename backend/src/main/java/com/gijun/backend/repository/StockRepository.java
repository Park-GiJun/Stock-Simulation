package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findAllByIsTrading(Boolean isTrading);

    Optional<Stock> findByStockCode(String stockCode);
}