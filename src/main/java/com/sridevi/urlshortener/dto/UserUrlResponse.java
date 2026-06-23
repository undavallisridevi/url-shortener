package com.sridevi.urlshortener.dto;

public record UserUrlResponse(
        String shortCode,
        String shortUrl,
        String originalUrl
) {
}