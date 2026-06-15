package com.sridevi.urlshortener.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record CreateUrlRequest(
        @JsonAlias("originalUrl")
        @NotBlank @Size(max = 4096) @Pattern(regexp = "https?://.+", message = "must be an absolute HTTP(S) URL") String url,
        @Pattern(regexp = "[A-Za-z0-9_-]{4,20}", message = "must be 4-20 URL-safe characters") String customAlias,
        @Min(1) @Max(3650) Integer expiryDays,
        @Future Instant expiresAt) {
    public Instant resolvedExpiry() {
        return expiresAt != null ? expiresAt : expiryDays == null ? null : Instant.now().plus(expiryDays, ChronoUnit.DAYS);
    }
}
