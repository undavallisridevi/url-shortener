package com.sridevi.urlshortener.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import com.sridevi.urlshortener.cache.CacheLock;
import com.sridevi.urlshortener.cache.CachedUrl;
import com.sridevi.urlshortener.cache.UrlCache;
import com.sridevi.urlshortener.dto.CreateUrlRequest;
import com.sridevi.urlshortener.dto.UrlResponse;
import com.sridevi.urlshortener.dto.UrlStatsResponse;
import com.sridevi.urlshortener.entity.Url;
import com.sridevi.urlshortener.entity.User;
import com.sridevi.urlshortener.exception.ExpiredUrlException;
import com.sridevi.urlshortener.exception.ForbiddenException;
import com.sridevi.urlshortener.kafka.producer.AnalyticsEventProducer;
import com.sridevi.urlshortener.mapper.UrlMapper;
import com.sridevi.urlshortener.repository.AnalyticsSummaryRepository;
import com.sridevi.urlshortener.repository.DailyClickRepository;
import com.sridevi.urlshortener.repository.UrlRepository;
import com.sridevi.urlshortener.repository.UserRepository;
import com.sridevi.urlshortener.util.Base62Encoder;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UrlServiceImplTest {
    @Mock UrlRepository urls; @Mock UserRepository users; @Mock UrlCache cache; @Mock CacheLock cacheLock;
    @Mock AnalyticsSummaryRepository analytics;
    @Mock AnalyticsEventProducer analyticsProducer;
    @Mock
     DailyClickRepository dailyClickRepository;
    
    UrlServiceImpl service;
    
    @BeforeEach
    void setUp() {

        lenient().when(analyticsProducer.publish(any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        service = new UrlServiceImpl(
                urls,
                users,
                new Base62Encoder(),
                new UrlMapper("http://localhost:8080"),
                cache,
                cacheLock,
                analytics,
                dailyClickRepository, analyticsProducer
        );
    }
    
    @Test void createsGeneratedCodeFromFetchedSequenceAndInsertsOnce() {
        User owner = user("alice");
        when(urls.nextSequenceValue()).thenReturn(62L);
        when(users.findByUsername("alice")).thenReturn(Optional.of(owner));
        when(urls.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UrlResponse response = service.create(new CreateUrlRequest("https://example.com/a", null, null, null), "alice");
        assertEquals("10", response.shortCode());
        assertEquals("http://localhost:8080/10", response.shortUrl());
        verify(urls).nextSequenceValue();
        verify(urls, times(1)).saveAndFlush(any());
    }

    @Test void returnsCachedRedirectWithoutDatabaseRead() {
        when(cache.get("abc1")).thenReturn(Optional.of(new CachedUrl("https://example.com", null)));
        assertEquals("https://example.com", service.resolve("abc1"));
        verifyNoInteractions(urls);
    }

    @Test void rejectsDeleteByAnotherUser() {
        Url url = new Url(); url.setUser(user("owner"));
        when(urls.findOwnedActiveUrl("abc1")).thenReturn(Optional.of(url));
        assertThrows(ForbiddenException.class, () -> service.delete("abc1", "other"));
        verify(cache, never()).evict(anyString());
    }

    @Test void rejectsExpiredCachedUrl() {
        when(cache.get("old1")).thenReturn(Optional.of(new CachedUrl("https://example.com", Instant.now().minusSeconds(1))));
        assertThrows(ExpiredUrlException.class, () -> service.resolve("old1"));
    }

    @Test void returnsZeroStatsBeforeAnalyticsAreRecorded() {
        Url url = new Url(); url.setUser(user("alice"));
        when(urls.findOwnedActiveUrl("abc1")).thenReturn(Optional.of(url));
        when(analytics.findById("abc1")).thenReturn(Optional.empty());
        UrlStatsResponse response = service.stats("abc1", "alice");
        assertEquals(0, response.totalClicks());
        assertNull(response.lastClickedAt());
    }

    private User user(String username) { User user = new User(); user.setUsername(username); return user; }
}
