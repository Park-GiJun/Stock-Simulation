package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.Institution;
import com.gijun.backend.domain.entity.InstitutionCharacteristics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionCharacteristicsRepository extends JpaRepository<InstitutionCharacteristics, Long> {
    Optional<InstitutionCharacteristics> findByInstitution(Institution institution);
}