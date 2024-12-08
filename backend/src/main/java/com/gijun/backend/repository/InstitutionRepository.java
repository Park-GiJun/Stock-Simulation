package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
}