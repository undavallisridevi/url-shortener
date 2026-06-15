package com.sridevi.urlshortener.mapper;

import com.sridevi.urlshortener.dto.UrlResponse;
import com.sridevi.urlshortener.entity.Url;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class UrlMapper {
    private final String baseUrl;

    public UrlMapper(@Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }
    public UrlResponse toResponse(Url url) {
        return new UrlResponse(url.getShortCode(), baseUrl + "/" + url.getShortCode(), url.getOriginalUrl(), url.isCustomAlias(),
                url.getExpiresAt(), url.getCreatedAt());
    }
}
