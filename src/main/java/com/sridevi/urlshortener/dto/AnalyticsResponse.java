package com.sridevi.urlshortener.dto;

import java.time.Instant;
import java.util.List;

public record AnalyticsResponse(
        String shortCode,
        long totalClicks,
        Instant lastClickedAt,
        List<DailyClickResponse> dailyClicks
) {
}