package com.sridevi.urlshortener.dto;

import java.time.LocalDate;

public record DailyClickResponse(
        LocalDate date,
        long count
) {
}