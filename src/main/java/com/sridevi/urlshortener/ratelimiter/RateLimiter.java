package com.sridevi.urlshortener.ratelimiter;

import java.time.Duration;

/** Placeholder contract for a future Redis-backed rate limiter. */
public interface RateLimiter {
    RateLimitDecision tryAcquire(String key, int limit, Duration window);
}
