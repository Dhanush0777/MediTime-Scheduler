package com.medtime.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdherenceStatsDto {

    private long todayTotal;
    private long todayTaken;
    private long todayPending;
    private long todayMissed;
    private double todayAdherencePercentage;

    private long overallTotal;
    private long overallTaken;
    private long overallPending;
    private long overallMissed;
    private double overallAdherencePercentage;

    private List<DailyAdherenceDto> recentDays = new ArrayList<>();

    public AdherenceStatsDto() {
    }

    public static class DailyAdherenceDto {
        private LocalDate date;
        private String dayLabel; // e.g. "Mon, Aug 31"
        private long total;
        private long taken;
        private long missed;
        private long pending;
        private double percentage;

        public DailyAdherenceDto() {
        }

        public DailyAdherenceDto(LocalDate date, String dayLabel, long total, long taken, long missed, long pending, double percentage) {
            this.date = date;
            this.dayLabel = dayLabel;
            this.total = total;
            this.taken = taken;
            this.missed = missed;
            this.pending = pending;
            this.percentage = percentage;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getDayLabel() {
            return dayLabel;
        }

        public void setDayLabel(String dayLabel) {
            this.dayLabel = dayLabel;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getTaken() {
            return taken;
        }

        public void setTaken(long taken) {
            this.taken = taken;
        }

        public long getMissed() {
            return missed;
        }

        public void setMissed(long missed) {
            this.missed = missed;
        }

        public long getPending() {
            return pending;
        }

        public void setPending(long pending) {
            this.pending = pending;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    public long getTodayTotal() {
        return todayTotal;
    }

    public void setTodayTotal(long todayTotal) {
        this.todayTotal = todayTotal;
    }

    public long getTodayTaken() {
        return todayTaken;
    }

    public void setTodayTaken(long todayTaken) {
        this.todayTaken = todayTaken;
    }

    public long getTodayPending() {
        return todayPending;
    }

    public void setTodayPending(long todayPending) {
        this.todayPending = todayPending;
    }

    public long getTodayMissed() {
        return todayMissed;
    }

    public void setTodayMissed(long todayMissed) {
        this.todayMissed = todayMissed;
    }

    public double getTodayAdherencePercentage() {
        return todayAdherencePercentage;
    }

    public void setTodayAdherencePercentage(double todayAdherencePercentage) {
        this.todayAdherencePercentage = todayAdherencePercentage;
    }

    public long getOverallTotal() {
        return overallTotal;
    }

    public void setOverallTotal(long overallTotal) {
        this.overallTotal = overallTotal;
    }

    public long getOverallTaken() {
        return overallTaken;
    }

    public void setOverallTaken(long overallTaken) {
        this.overallTaken = overallTaken;
    }

    public long getOverallPending() {
        return overallPending;
    }

    public void setOverallPending(long overallPending) {
        this.overallPending = overallPending;
    }

    public long getOverallMissed() {
        return overallMissed;
    }

    public void setOverallMissed(long overallMissed) {
        this.overallMissed = overallMissed;
    }

    public double getOverallAdherencePercentage() {
        return overallAdherencePercentage;
    }

    public void setOverallAdherencePercentage(double overallAdherencePercentage) {
        this.overallAdherencePercentage = overallAdherencePercentage;
    }

    public List<DailyAdherenceDto> getRecentDays() {
        return recentDays;
    }

    public void setRecentDays(List<DailyAdherenceDto> recentDays) {
        this.recentDays = recentDays;
    }
}
