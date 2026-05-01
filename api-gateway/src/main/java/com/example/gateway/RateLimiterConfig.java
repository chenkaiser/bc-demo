package com.example.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    /**
     * Keys the rate limiter bucket by the authenticated user's JWT subject claim.
     * Each user gets their own independent token bucket — a user flooding requests
     * cannot affect the quota of other users.
     *
     * In practice this resolver is only reached for authenticated routes (the security
     * filter rejects unauthenticated requests before route filters run), so the
     * "anonymous" fallback is a safety net for any future public routes.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(principal -> "user:" + principal.getName())
                .defaultIfEmpty("user:anonymous");
    }
}
