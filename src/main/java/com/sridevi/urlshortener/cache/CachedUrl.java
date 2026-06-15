package com.sridevi.urlshortener.cache;

import java.io.Serializable;
import java.time.Instant;

public record CachedUrl(String originalUrl, Instant expiresAt) implements Serializable {}
