package com.sridevi.urlshortener.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "analytics_summary")
public class AnalyticsSummary {
    @Id @Column(name = "short_code", length = 20) private String shortCode;
    @Column(name = "total_clicks", nullable = false) private long totalClicks;
    @Column(name = "last_clicked_at") private Instant lastClickedAt;
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(long totalClicks) { this.totalClicks = totalClicks; }
    public Instant getLastClickedAt() { return lastClickedAt; }
    public void setLastClickedAt(Instant lastClickedAt) { this.lastClickedAt = lastClickedAt; }
}
