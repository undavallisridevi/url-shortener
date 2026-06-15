package com.sridevi.urlshortener.cache;

import java.util.Optional;

public interface UrlCache {
    Optional<CachedUrl> get(String shortCode);
    void put(String shortCode, CachedUrl url);
    void evict(String shortCode);
}
