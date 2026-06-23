package com.sridevi.urlshortener.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sridevi.urlshortener.entity.AnalyticsSummary;

public interface AnalyticsSummaryRepository extends JpaRepository<AnalyticsSummary, String> {
	
	List<AnalyticsSummary> findAllByOrderByTotalClicksDesc(
	        Pageable pageable);
}
