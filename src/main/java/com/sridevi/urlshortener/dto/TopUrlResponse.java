package com.sridevi.urlshortener.dto;

public record TopUrlResponse(
        String shortCode,
        long totalClicks
) {
}