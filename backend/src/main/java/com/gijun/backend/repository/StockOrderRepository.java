package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.StockOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockOrderRepository extends JpaRepository<StockOrder, Long> {
}