package com.example.backend.aspect;

import com.example.backend.annotation.RateLimit;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.AuthResponse;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Around("@annotation(rateLimit) && within(com.example.backend.controller.AuthController)")
    public Object rateLimitAuthHandler(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimit.value());

        return ((Mono<ResponseEntity<AuthResponse>>) joinPoint.proceed())
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(throwable -> {
                    if (throwable instanceof io.github.resilience4j.ratelimiter.RequestNotPermitted) {
                        return Mono.just(ResponseEntity.status(429).body(
                                AuthResponse.builder()
                                        .status("error")
                                        .message("Too many requests. Please try again later.")
                                        .build()
                        ));
                    }
                    return Mono.error(throwable);
                });
    }

    @Around("@annotation(rateLimit) && within(com.example.backend.controller.UserController)")
    public Object rateLimitUserHandler(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimit.value());

        return ((Mono<ApiResponse<?>>) joinPoint.proceed())
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(throwable -> {
                    if (throwable instanceof io.github.resilience4j.ratelimiter.RequestNotPermitted) {
                        return Mono.just(ApiResponse.builder()
                                .status("error")
                                .message("Too many requests. Please try again later.")
                                .build());
                    }
                    return Mono.error(throwable);
                });
    }
} 