package com.sridevi.urlshortener.ratelimiter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimiterProperties(
        int perIpLimit,
        int perUserLimit,
        Duration window
) {
}