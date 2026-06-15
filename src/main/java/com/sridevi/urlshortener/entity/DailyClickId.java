package com.sridevi.urlshortener.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class DailyClickId implements Serializable {
    private String shortCode;
    private LocalDate clickDate;
    public DailyClickId() {}
    public DailyClickId(String shortCode, LocalDate clickDate) { this.shortCode = shortCode; this.clickDate = clickDate; }
    public String getShortCode() { return shortCode; }
    public LocalDate getClickDate() { return clickDate; }
    @Override public boolean equals(Object other) { return this == other || other instanceof DailyClickId that && Objects.equals(shortCode, that.shortCode) && Objects.equals(clickDate, that.clickDate); }
    @Override public int hashCode() { return Objects.hash(shortCode, clickDate); }
}
