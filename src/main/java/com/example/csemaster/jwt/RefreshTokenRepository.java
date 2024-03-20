package com.example.csemaster.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<ManagerRefreshTokenEntity, String> {
    Optional<ManagerRefreshTokenEntity> findById(String managerId);
}
