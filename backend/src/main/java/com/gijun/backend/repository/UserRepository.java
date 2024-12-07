package com.gijun.backend.repository;

import com.gijun.backend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
}
