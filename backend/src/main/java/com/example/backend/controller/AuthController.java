package com.example.backend.controller;

import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.TokenRefreshRequest;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.example.backend.annotation.RateLimit;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @RateLimit("auth")
    public Mono<ResponseEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(AuthResponse.builder()
                                .status("error")
                                .message(e.getMessage())
                                .build())));
    }

    @PostMapping("/login")
    @RateLimit("auth")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(AuthResponse.builder()
                                .status("error")
                                .message(e.getMessage())
                                .build())));
    }

    @PostMapping("/refresh")
    @RateLimit("auth")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request.getRefreshToken())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(AuthResponse.builder()
                                .status("error")
                                .message(e.getMessage())
                                .build())));
    }
} 