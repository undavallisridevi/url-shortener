package com.sridevi.urlshortener.service;

import java.util.List;

import com.sridevi.urlshortener.dto.AnalyticsResponse;
import com.sridevi.urlshortener.dto.CreateUrlRequest;
import com.sridevi.urlshortener.dto.TopUrlResponse;
import com.sridevi.urlshortener.dto.UrlResponse;
import com.sridevi.urlshortener.dto.UrlStatsResponse;
import com.sridevi.urlshortener.dto.UserUrlResponse;

public interface UrlService {
    UrlResponse create(CreateUrlRequest request, String username);
    String resolve(String shortCode);
    UrlStatsResponse stats(String shortCode, String username);
    void delete(String shortCode, String username);
    List<UserUrlResponse> getMyUrls(String username);
    AnalyticsResponse analytics(
            String shortCode,
            String username
    );
    List<TopUrlResponse> topUrls();
}
