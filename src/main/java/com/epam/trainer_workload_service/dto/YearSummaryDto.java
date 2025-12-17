package com.epam.trainer_workload_service.dto;

import java.util.List;
import java.util.Objects;

public class YearSummaryDto {
    private int year;
    private List<MonthSummaryDto> months;

    public YearSummaryDto() {
    }

    public YearSummaryDto(int year, List<MonthSummaryDto> months) {
        this.year = year;
        this.months = months;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<MonthSummaryDto> getMonths() {
        return months;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        YearSummaryDto that = (YearSummaryDto) object;
        return year == that.year;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(year);
    }
}
