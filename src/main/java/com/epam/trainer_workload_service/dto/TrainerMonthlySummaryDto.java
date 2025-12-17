package com.epam.trainer_workload_service.dto;

import java.util.Objects;

public class TrainerMonthlySummaryDto {
    private String username;
    private int year;
    private int month;

    public TrainerMonthlySummaryDto(){}

    public TrainerMonthlySummaryDto(int year, String username, int month) {
        this.year = year;
        this.username = username;
        this.month = month;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TrainerMonthlySummaryDto that = (TrainerMonthlySummaryDto) object;
        return year == that.year && month == that.month && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, year, month);
    }
}
