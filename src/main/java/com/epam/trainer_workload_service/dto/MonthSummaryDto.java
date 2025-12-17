package com.epam.trainer_workload_service.dto;

import java.util.Objects;

public class MonthSummaryDto {
    private int month;
    private long totalMinutes;

    public MonthSummaryDto() {
    }

    public MonthSummaryDto(int month, long totalMinutes) {
        this.month = month;
        this.totalMinutes = totalMinutes;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public long getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(long totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        MonthSummaryDto that = (MonthSummaryDto) object;
        return month == that.month && totalMinutes == that.totalMinutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(month, totalMinutes);
    }
}
