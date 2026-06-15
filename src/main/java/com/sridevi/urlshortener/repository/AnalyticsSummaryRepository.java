package com.sridevi.urlshortener.repository;

import com.sridevi.urlshortener.entity.AnalyticsSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsSummaryRepository extends JpaRepository<AnalyticsSummary, String> {}
