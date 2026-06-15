package com.sridevi.urlshortener.dto;

import java.time.Instant;

public record UrlStatsResponse(String shortCode, long totalClicks, Instant lastClickedAt) {}
