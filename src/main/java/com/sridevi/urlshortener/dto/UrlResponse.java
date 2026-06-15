package com.sridevi.urlshortener.dto;

import java.time.Instant;

public record UrlResponse(String shortCode, String shortUrl, String originalUrl, boolean customAlias,
                          Instant expiresAt, Instant createdAt) {}
