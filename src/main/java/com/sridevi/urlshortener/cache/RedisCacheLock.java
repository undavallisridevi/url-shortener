package com.sridevi.urlshortener.cache;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.*;

@Component
public class RedisCacheLock implements CacheLock {
    private static final Logger log = LoggerFactory.getLogger(RedisCacheLock.class);
    private static final String PREFIX = "lock:url:";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Long.class);
    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RedisCacheLock(StringRedisTemplate redis, @Value("${app.cache.lock-ttl}") Duration ttl) {
        this.redis = redis; this.ttl = ttl;
    }
    @Override public Optional<String> tryLock(String shortCode) {
        String token = UUID.randomUUID().toString();
        try { return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(PREFIX + shortCode, token, ttl)) ? Optional.of(token) : Optional.empty(); }
        catch (RuntimeException ex) { log.warn("Redis lock acquisition failed for {}", shortCode, ex); return Optional.empty(); }
    }
    @Override public void unlock(String shortCode, String token) {
        try { redis.execute(UNLOCK_SCRIPT, List.of(PREFIX + shortCode), token); }
        catch (RuntimeException ex) { log.warn("Redis lock release failed for {}", shortCode, ex); }
    }
}
