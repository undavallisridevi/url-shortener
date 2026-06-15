package com.sridevi.urlshortener.cache;

import java.util.Optional;

public interface CacheLock {
    Optional<String> tryLock(String shortCode);
    void unlock(String shortCode, String token);
}
