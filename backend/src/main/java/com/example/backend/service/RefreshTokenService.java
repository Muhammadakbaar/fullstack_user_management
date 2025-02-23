package com.example.backend.service;

import com.example.backend.model.RefreshToken;
import com.example.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    public Mono<RefreshToken> createRefreshToken(UUID userId) {
        return refreshTokenRepository.deleteByUserId(userId)
                .then(Mono.just(RefreshToken.builder()
                        .userId(userId)
                        .token(UUID.randomUUID().toString())
                        .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                        .build()))
                .flatMap(refreshTokenRepository::save);
    }

    public Mono<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public Mono<RefreshToken> verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            return refreshTokenRepository.delete(token)
                    .then(Mono.error(new RuntimeException("Refresh token was expired")));
        }
        return Mono.just(token);
    }
} 