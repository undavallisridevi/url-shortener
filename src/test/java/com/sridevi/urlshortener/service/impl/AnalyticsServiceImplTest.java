package com.sridevi.urlshortener.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sridevi.urlshortener.entity.AnalyticsSummary;
import com.sridevi.urlshortener.entity.DailyClick;
import com.sridevi.urlshortener.repository.AnalyticsSummaryRepository;
import com.sridevi.urlshortener.repository.DailyClickRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private AnalyticsSummaryRepository analyticsRepository;

    @Mock
    private DailyClickRepository dailyClickRepository;

    private AnalyticsServiceImpl service;

    @BeforeEach
    void setup() {
        service = new AnalyticsServiceImpl(
                analyticsRepository,
                dailyClickRepository
        );
    }

    @Test
    void createsAnalyticsForFirstClick() {

        when(analyticsRepository.findById("abc"))
                .thenReturn(Optional.empty());

        service.recordClick("abc");

        ArgumentCaptor<AnalyticsSummary> summaryCaptor =
                ArgumentCaptor.forClass(AnalyticsSummary.class);

        verify(analyticsRepository).save(summaryCaptor.capture());

        AnalyticsSummary saved = summaryCaptor.getValue();

        assertEquals("abc", saved.getShortCode());
        assertEquals(1, saved.getTotalClicks());
        assertNotNull(saved.getLastClickedAt());

        verify(dailyClickRepository).save(any(DailyClick.class));
    }
    
    @Test
    void incrementsExistingAnalytics() {

        AnalyticsSummary summary = new AnalyticsSummary();
        summary.setShortCode("abc");
        summary.setTotalClicks(10);

        when(analyticsRepository.findById("abc"))
                .thenReturn(Optional.of(summary));

        service.recordClick("abc");

        assertEquals(11, summary.getTotalClicks());

        verify(analyticsRepository).save(summary);
    }
}