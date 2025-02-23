package com.example.backend.repository;

import com.example.backend.model.RefreshToken;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RefreshTokenRepository extends R2dbcRepository<RefreshToken, UUID> {
    Mono<RefreshToken> findByToken(String token);
    Mono<RefreshToken> findByUserId(UUID userId);
    
    @Modifying
    @Query("DELETE FROM refresh_tokens WHERE user_id = :userId")
    Mono<Void> deleteByUserId(UUID userId);
} 