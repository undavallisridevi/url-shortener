package com.sridevi.urlshortener.cache;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Optional;

@Component
public class RedisUrlCache implements UrlCache {
    private static final Logger log = LoggerFactory.getLogger(RedisUrlCache.class);
    private static final String PREFIX = "url:";
    private final RedisTemplate<String, CachedUrl> redisTemplate;
    private final Duration ttl;

    public RedisUrlCache(RedisTemplate<String, CachedUrl> redisTemplate,
                         @Value("${app.cache.url-ttl}") Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
    }
    public Optional<CachedUrl> get(String shortCode) {
        try { return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + shortCode)); }
        catch (RuntimeException ex) { log.warn("Redis read failed for {}", shortCode, ex); return Optional.empty(); }
    
//        catch (RuntimeException ex) {
//            ex.printStackTrace();
//            throw ex;
//        }
    }
    public void put(String shortCode, CachedUrl url) {
        try {
            Duration effectiveTtl = url.expiresAt() == null ? ttl : min(ttl, Duration.between(java.time.Instant.now(), url.expiresAt()));
            if (!effectiveTtl.isNegative() && !effectiveTtl.isZero()) redisTemplate.opsForValue().set(PREFIX + shortCode, url, effectiveTtl);
        } 
        catch (RuntimeException ex) { log.warn("Redis write failed for {}", shortCode, ex); }
    
//        catch (RuntimeException ex) {
//            ex.printStackTrace();
//            throw ex;
//        }
    }
    public void evict(String shortCode) {
        try { redisTemplate.delete(PREFIX + shortCode); }
        catch (RuntimeException ex) { log.warn("Redis eviction failed for {}", shortCode, ex); }
    }
    private Duration min(Duration first, Duration second) { return first.compareTo(second) <= 0 ? first : second; }
}
