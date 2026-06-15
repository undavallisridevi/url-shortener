package com.sridevi.urlshortener.kafka.event;

import java.time.Instant;

public record UrlClickedEvent(String shortCode, Instant timestamp, String ip, String userAgent) {}
