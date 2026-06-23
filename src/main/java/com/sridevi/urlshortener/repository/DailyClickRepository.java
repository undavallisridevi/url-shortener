package com.sridevi.urlshortener.repository;

import com.sridevi.urlshortener.entity.DailyClick;
import com.sridevi.urlshortener.entity.DailyClickId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyClickRepository
        extends JpaRepository<DailyClick, DailyClickId> {

    List<DailyClick> findByShortCodeOrderByClickDateAsc(
            String shortCode
    );
}