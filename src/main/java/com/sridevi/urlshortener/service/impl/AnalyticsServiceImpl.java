package com.sridevi.urlshortener.service.impl;

import com.sridevi.urlshortener.entity.*;
import com.sridevi.urlshortener.repository.*;
import com.sridevi.urlshortener.service.AnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
public class AnalyticsServiceImpl
        implements AnalyticsService {

    private final AnalyticsSummaryRepository summaryRepo;
    private final DailyClickRepository dailyRepo;

    public AnalyticsServiceImpl(
            AnalyticsSummaryRepository summaryRepo,
            DailyClickRepository dailyRepo
    ) {
        this.summaryRepo = summaryRepo;
        this.dailyRepo = dailyRepo;
    }

    @Override
    @Transactional
    public void recordClick(
            String shortCode
    ) {

        AnalyticsSummary summary =
                summaryRepo
                        .findById(shortCode)
                        .orElseGet(() -> {

                            AnalyticsSummary s =
                                    new AnalyticsSummary();

                            s.setShortCode(shortCode);
                            s.setTotalClicks(0);

                            return s;
                        });

        summary.setTotalClicks(
                summary.getTotalClicks() + 1
        );

        summary.setLastClickedAt(
                Instant.now()
        );

        summaryRepo.save(summary);

        LocalDate today =
                LocalDate.now(ZoneOffset.UTC);

        DailyClickId id =
                new DailyClickId(
                        shortCode,
                        today
                );

        DailyClick daily =
                dailyRepo
                        .findById(id)
                        .orElseGet(() -> {

                            DailyClick d =
                                    new DailyClick();

                            d.setShortCode(shortCode);
                            d.setClickDate(today);
                            d.setClickCount(0);

                            return d;
                        });

        daily.setClickCount(
                daily.getClickCount() + 1
        );

        dailyRepo.save(daily);
    }
}