package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findAllByIsTrading(Boolean isTrading);
}