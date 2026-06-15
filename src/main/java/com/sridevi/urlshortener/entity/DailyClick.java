package com.sridevi.urlshortener.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_clicks")
@IdClass(DailyClickId.class)
public class DailyClick {
    @Id @Column(name = "short_code", length = 20) private String shortCode;
    @Id @Column(name = "click_date") private LocalDate clickDate;
    @Column(name = "click_count", nullable = false) private long clickCount;
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public LocalDate getClickDate() { return clickDate; }
    public void setClickDate(LocalDate clickDate) { this.clickDate = clickDate; }
    public long getClickCount() { return clickCount; }
    public void setClickCount(long clickCount) { this.clickCount = clickCount; }
}
