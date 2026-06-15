package com.sridevi.urlshortener.repository;

import com.sridevi.urlshortener.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyClickRepository extends JpaRepository<DailyClick, DailyClickId> {}
