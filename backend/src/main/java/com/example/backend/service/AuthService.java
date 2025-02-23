package com.example.backend.service;

import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.model.User;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.example.backend.exception.UnauthorizedException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final ReactiveAuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public Mono<AuthResponse> register(RegisterRequest request) {
        return Mono.zip(
                userRepository.existsByUsername(request.getUsername()),
                userRepository.existsByEmail(request.getEmail())
        ).flatMap(tuple -> {
            boolean usernameExists = tuple.getT1();
            boolean emailExists = tuple.getT2();
            
            if (usernameExists) {
                return Mono.error(new RuntimeException("Username already exists"));
            }
            if (emailExists) {
                return Mono.error(new RuntimeException("Email already exists"));
            }
            
            return roleRepository.findByName("ROLE_USER")
                    .switchIfEmpty(Mono.error(new RuntimeException("Role not found")))
                    .flatMap(role -> {
                        log.info("Found role: {}", role);
                        User user = new User();
                        user.setUsername(request.getUsername());
                        user.setEmail(request.getEmail());
                        user.setPassword(passwordEncoder.encode(request.getPassword()));
                        user.setRoleId(role.getId());
                        return userRepository.save(user)
                                .map(savedUser -> new Object[]{savedUser, role});
                    })
                    .doOnNext(arr -> log.info("User saved: {}", arr[0]))
                    .flatMap(arr -> {
                        User user = (User) arr[0];
                        com.example.backend.model.Role role = (com.example.backend.model.Role) arr[1];
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
                        return authenticationManager.authenticate(authToken)
                                .flatMap(auth -> refreshTokenService.createRefreshToken(user.getId())
                                        .map(refreshToken -> AuthResponse.builder()
                                                .status("success")
                                                .message("Registration successful")
                                                .data(AuthResponse.AuthData.builder()
                                                        .token(tokenProvider.generateToken(auth))
                                                        .refreshToken(refreshToken.getToken())
                                                        .tokenType("Bearer")
                                                        .user(AuthResponse.UserInfo.builder()
                                                                .username(user.getUsername())
                                                                .email(user.getEmail())
                                                                .role(role.getName())
                                                                .build())
                                                        .build())
                                                .build()));
                    });
        });
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new UnauthorizedException("Invalid username or password")))
                .flatMap(user -> roleRepository.findById(user.getRoleId())
                        .flatMap(role -> {
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
                            return authenticationManager.authenticate(authToken)
                                    .onErrorResume(e -> Mono.error(new UnauthorizedException("Invalid username or password")))
                                    .flatMap(auth -> refreshTokenService.createRefreshToken(user.getId())
                                            .map(refreshToken -> AuthResponse.builder()
                                                    .status("success")
                                                    .message("Login successful")
                                                    .data(AuthResponse.AuthData.builder()
                                                            .token(tokenProvider.generateToken(auth))
                                                            .refreshToken(refreshToken.getToken())
                                                            .tokenType("Bearer")
                                                            .user(AuthResponse.UserInfo.builder()
                                                                    .username(user.getUsername())
                                                                    .email(user.getEmail())
                                                                    .role(role.getName())
                                                                    .build())
                                                            .build())
                                                    .build()));
                        }));
    }

    public Mono<AuthResponse> refreshToken(String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Invalid refresh token")))
                .flatMap(refreshTokenService::verifyExpiration)
                .onErrorResume(e -> Mono.error(new UnauthorizedException("Refresh token was expired")))
                .flatMap(token -> userRepository.findById(token.getUserId()))
                .flatMap(user -> roleRepository.findById(user.getRoleId())
                        .map(role -> AuthResponse.builder()
                                .status("success")
                                .message("Token refreshed successfully")
                                .data(AuthResponse.AuthData.builder()
                                        .token(tokenProvider.generateTokenFromUsername(user.getUsername()))
                                        .refreshToken(refreshToken)
                                        .tokenType("Bearer")
                                        .user(AuthResponse.UserInfo.builder()
                                                .username(user.getUsername())
                                                .email(user.getEmail())
                                                .role(role.getName())
                                                .build())
                                        .build())
                                .build()));
    }
} 