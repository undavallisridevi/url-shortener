package com.sridevi.urlshortener.service.impl;

import com.sridevi.urlshortener.cache.*;
import com.sridevi.urlshortener.dto.*;
import com.sridevi.urlshortener.entity.*;
import com.sridevi.urlshortener.exception.*;
import com.sridevi.urlshortener.mapper.UrlMapper;
import com.sridevi.urlshortener.repository.*;
import com.sridevi.urlshortener.util.Base62Encoder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UrlServiceImplTest {
    @Mock UrlRepository urls; @Mock UserRepository users; @Mock UrlCache cache; @Mock CacheLock cacheLock;
    @Mock AnalyticsSummaryRepository analytics;
    UrlServiceImpl service;
    @BeforeEach void setUp() { service = new UrlServiceImpl(urls, users, new Base62Encoder(), new UrlMapper("http://localhost:8080"), cache, cacheLock, analytics); }

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
