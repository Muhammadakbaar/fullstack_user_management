package com.example.backend.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(10)          // 10 requests
                .limitRefreshPeriod(Duration.ofMinutes(1))  // per minute
                .timeoutDuration(Duration.ofMillis(500))    // wait 500ms for permission
                .build();
        
        return RateLimiterRegistry.of(config);
    }

    @Bean
    public RateLimiter authRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("auth");
    }

    @Bean
    public RateLimiter userRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("user");
    }
} 