package com.sridevi.urlshortener.service;

public interface AnalyticsService {

    void recordClick(
            String shortCode
    );
}