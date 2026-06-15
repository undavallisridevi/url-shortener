package com.sridevi.urlshortener.dto;

public record AuthResponse(String token, String tokenType, long expiresInSeconds) {}
