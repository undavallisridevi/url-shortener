package com.sridevi.urlshortener.ratelimiter;

public record RateLimitDecision(boolean allowed, long remaining, long retryAfterSeconds) {}
