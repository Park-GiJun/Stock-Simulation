package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {
}