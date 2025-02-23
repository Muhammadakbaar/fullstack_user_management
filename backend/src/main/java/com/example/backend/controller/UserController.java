package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.UserResponse;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.example.backend.annotation.RateLimit;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping("/me")
    @RateLimit("user")
    public Mono<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Mono.error(new UnauthorizedException("Not authenticated"));
        }

        return userRepository.findByUsername(principal.getUsername())
                .switchIfEmpty(Mono.error(new UnauthorizedException("User not found")))
                .flatMap(user -> roleRepository.findById(user.getRoleId())
                        .switchIfEmpty(Mono.error(new UnauthorizedException("Role not found")))
                        .map(role -> ApiResponse.<UserResponse>builder()
                                .status("success")
                                .message("User details retrieved successfully")
                                .data(UserResponse.builder()
                                        .id(user.getId())
                                        .username(user.getUsername())
                                        .email(user.getEmail())
                                        .role(role.getName())
                                        .build())
                                .build()));
    }
}