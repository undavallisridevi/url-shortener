package com.sridevi.urlshortener.service.impl;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sridevi.urlshortener.cache.CacheLock;
import com.sridevi.urlshortener.cache.CachedUrl;
import com.sridevi.urlshortener.cache.UrlCache;
import com.sridevi.urlshortener.dto.AnalyticsResponse;
import com.sridevi.urlshortener.dto.CreateUrlRequest;
import com.sridevi.urlshortener.dto.DailyClickResponse;
import com.sridevi.urlshortener.dto.TopUrlResponse;
import com.sridevi.urlshortener.dto.UrlResponse;
import com.sridevi.urlshortener.dto.UrlStatsResponse;
import com.sridevi.urlshortener.dto.UserUrlResponse;
import com.sridevi.urlshortener.entity.Url;
import com.sridevi.urlshortener.entity.User;
import com.sridevi.urlshortener.exception.BadRequestException;
import com.sridevi.urlshortener.exception.ConflictException;
import com.sridevi.urlshortener.exception.ExpiredUrlException;
import com.sridevi.urlshortener.exception.ForbiddenException;
import com.sridevi.urlshortener.exception.ResourceNotFoundException;
import com.sridevi.urlshortener.kafka.event.UrlClickedEvent;
import com.sridevi.urlshortener.kafka.producer.AnalyticsEventProducer;
import com.sridevi.urlshortener.mapper.UrlMapper;
import com.sridevi.urlshortener.repository.AnalyticsSummaryRepository;
import com.sridevi.urlshortener.repository.DailyClickRepository;
import com.sridevi.urlshortener.repository.UrlRepository;
import com.sridevi.urlshortener.repository.UserRepository;
import com.sridevi.urlshortener.service.UrlService;
import com.sridevi.urlshortener.util.Base62Encoder;

@Service
public class UrlServiceImpl implements UrlService {
    private final UrlRepository urls; private final UserRepository users; private final Base62Encoder encoder;
    private final UrlMapper mapper; private final UrlCache cache; private final CacheLock cacheLock;
    private final AnalyticsSummaryRepository analytics;
    private final AnalyticsEventProducer analyticsProducer;
    private final DailyClickRepository dailyClicks;
    public UrlServiceImpl(UrlRepository urls, UserRepository users, Base62Encoder encoder, UrlMapper mapper,
                          UrlCache cache, CacheLock cacheLock, AnalyticsSummaryRepository analytics,DailyClickRepository dailyClicks, AnalyticsEventProducer analyticsProducer
                          ) {
        this.urls = urls; this.users = users; this.encoder = encoder; this.mapper = mapper;
        this.cache = cache; this.cacheLock = cacheLock; this.analytics = analytics;
		this.analyticsProducer = analyticsProducer;
		this.dailyClicks = dailyClicks;
    }
    @Override @Transactional
    public UrlResponse create(CreateUrlRequest request, String username) {
        validateUrl(request.url());
        long id = urls.nextSequenceValue();
        String alias = normalize(request.customAlias());
        String shortCode = alias == null ? encoder.encode(id) : alias;
        if (alias != null && urls.existsByShortCode(alias)) throw new ConflictException("Custom alias is already in use");
        User owner = users.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Url url = new Url(); url.setId(id); url.setShortCode(shortCode); url.setOriginalUrl(request.url());
        url.setUser(owner); url.setCustomAlias(alias != null); url.setExpiresAt(request.resolvedExpiry());
        try { return mapper.toResponse(urls.saveAndFlush(url)); }
        catch (DataIntegrityViolationException ex) { throw new ConflictException("Short code is already in use"); }
    }
    @Override
    @Transactional(readOnly = true)
    public String resolve(String shortCode) {

        CachedUrl cached = cache.get(shortCode).orElse(null);

        if (cached != null) {

            ensureNotExpired(cached.expiresAt());

            analyticsProducer.publish(
                    new UrlClickedEvent(
                            shortCode,
                            Instant.now(),
                            null,
                            null
                    )
            );

            return cached.originalUrl();
        }

        String lockToken = cacheLock.tryLock(shortCode).orElse(null);

        try {

            if (lockToken != null) {

                CachedUrl afterLock = cache.get(shortCode).orElse(null);

                if (afterLock != null) {

                    ensureNotExpired(afterLock.expiresAt());

                    analyticsProducer.publish(
                            new UrlClickedEvent(
                                    shortCode,
                                    Instant.now(),
                                    null,
                                    null
                            )
                    );

                    return afterLock.originalUrl();
                }
            }

            Url url = urls.findByShortCodeAndDeletedFalse(shortCode)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Short URL not found"));

            ensureNotExpired(url.getExpiresAt());

            cache.put(
                    shortCode,
                    new CachedUrl(
                            url.getOriginalUrl(),
                            url.getExpiresAt()
                    )
            );

            analyticsProducer.publish(
                    new UrlClickedEvent(
                            shortCode,
                            Instant.now(),
                            null,
                            null
                    )
            );

            return url.getOriginalUrl();

        } finally {

            if (lockToken != null) {
                cacheLock.unlock(shortCode, lockToken);
            }
        }
    }
    @Override @Transactional(readOnly = true)
    public UrlStatsResponse stats(String shortCode, String username) {
        Url url = urls.findOwnedActiveUrl(shortCode).orElseThrow(() -> new ResourceNotFoundException("Short URL not found"));
        if (!url.getUser().getUsername().equals(username)) throw new ForbiddenException("You do not own this short URL");
        return analytics.findById(shortCode)
                .map(summary -> new UrlStatsResponse(shortCode, summary.getTotalClicks(), summary.getLastClickedAt()))
                .orElseGet(() -> new UrlStatsResponse(shortCode, 0, null));
    }
    @Override @Transactional
    public void delete(String shortCode, String username) {
        Url url = urls.findOwnedActiveUrl(shortCode).orElseThrow(() -> new ResourceNotFoundException("Short URL not found"));
        if (!url.getUser().getUsername().equals(username)) throw new ForbiddenException("You do not own this short URL");
        url.setDeleted(true); url.setDeletedAt(Instant.now()); urls.save(url); cache.evict(shortCode);
    }
    private void ensureNotExpired(Instant expiry) { if (expiry != null && !expiry.isAfter(Instant.now())) throw new ExpiredUrlException("Short URL has expired"); }
    private String normalize(String alias) { return alias == null || alias.isBlank() ? null : alias; }
    private void validateUrl(String value) {
        try { URI uri = URI.create(value); if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) throw new IllegalArgumentException(); }
        catch (IllegalArgumentException ex) { throw new BadRequestException("originalUrl must be a valid absolute HTTP(S) URL"); }
    }
    @Override
    @Transactional(readOnly = true)
    public List<UserUrlResponse> getMyUrls(String username) {

        return urls.findByUserUsernameAndDeletedFalse(username)
                .stream()
                .map(url -> new UserUrlResponse(
                        url.getShortCode(),
                        "http://localhost:8080/" + url.getShortCode(),
                        url.getOriginalUrl()
                ))
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse analytics(
            String shortCode,
            String username
    ) {

        Url url =
                urls.findOwnedActiveUrl(shortCode)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Short URL not found"
                                ));

        if (!url.getUser().getUsername().equals(username)) {
            throw new ForbiddenException(
                    "You do not own this short URL"
            );
        }

        var summary =
                analytics.findById(shortCode)
                        .orElse(null);

        var daily =
                dailyClicks
                        .findByShortCodeOrderByClickDateAsc(
                                shortCode
                        )
                        .stream()
                        .map(d ->
                                new DailyClickResponse(
                                        d.getClickDate(),
                                        d.getClickCount()
                                ))
                        .toList();

        return new AnalyticsResponse(
                shortCode,
                summary == null ? 0 : summary.getTotalClicks(),
                summary == null ? null : summary.getLastClickedAt(),
                daily
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TopUrlResponse> topUrls() {

        return analytics
                .findAllByOrderByTotalClicksDesc(
                        PageRequest.of(0, 10)
                )
                .stream()
                .map(a ->
                        new TopUrlResponse(
                                a.getShortCode(),
                                a.getTotalClicks()
                        )
                )
                .toList();
    }
}
