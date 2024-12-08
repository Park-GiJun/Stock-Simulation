package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.InstitutionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionTransactionRepository extends JpaRepository<InstitutionTransaction, Long> {
}