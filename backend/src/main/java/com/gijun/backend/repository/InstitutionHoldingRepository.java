package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.Institution;
import com.gijun.backend.domain.entity.InstitutionHolding;
import com.gijun.backend.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstitutionHoldingRepository extends JpaRepository<InstitutionHolding, Long> {
    List<InstitutionHolding> findByInstitution(Institution institution);
    Optional<InstitutionHolding> findByInstitutionAndStock(Institution institution, Stock stock);
}