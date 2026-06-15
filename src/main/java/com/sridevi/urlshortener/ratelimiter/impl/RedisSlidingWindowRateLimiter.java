package com.sridevi.urlshortener.ratelimiter.impl;

import com.sridevi.urlshortener.ratelimiter.RateLimitDecision;
import com.sridevi.urlshortener.ratelimiter.RateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Component
public class RedisSlidingWindowRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;

    public RedisSlidingWindowRateLimiter(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            local member = ARGV[4]

            redis.call('ZREMRANGEBYSCORE', key, '-inf', now-window)

            local count = redis.call('ZCARD', key)

            if count >= limit then
                return count
            end

            redis.call('ZADD', key, now, member)
            redis.call('EXPIRE', key, math.ceil(window))

            return count + 1
            """;

    @Override
    public RateLimitDecision tryAcquire(
            String key,
            int limit,
            Duration window
    ) {

        long now = Instant.now().getEpochSecond();

        String member =
                now + ":" + UUID.randomUUID();

        DefaultRedisScript<Long> script =
                new DefaultRedisScript<>();

        script.setScriptText(LUA_SCRIPT);
        script.setResultType(Long.class);

        Long result =
                redisTemplate.execute(
                        script,
                        Collections.singletonList(key),
                        String.valueOf(now),
                        String.valueOf(window.getSeconds()),
                        String.valueOf(limit),
                        member
                );

        long count = result == null ? 0 : result;

        boolean allowed = count < limit;

        long remaining =
                Math.max(0, limit - count);

        return new RateLimitDecision(
                allowed,
                remaining,
                window.getSeconds()
        );
    }
}