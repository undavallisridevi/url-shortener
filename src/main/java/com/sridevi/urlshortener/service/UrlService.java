package com.sridevi.urlshortener.service;

import com.sridevi.urlshortener.dto.*;

public interface UrlService {
    UrlResponse create(CreateUrlRequest request, String username);
    String resolve(String shortCode);
    UrlStatsResponse stats(String shortCode, String username);
    void delete(String shortCode, String username);
}
