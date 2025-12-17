package com.epam.trainer_workload_service.dto;

import java.util.List;
import java.util.Objects;

public class TrainingSummaryDto {
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private List<YearSummaryDto> years;

    public TrainingSummaryDto() {
    }

    public TrainingSummaryDto(String username, String firstName, String lastName, boolean isActive, List<YearSummaryDto> years) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.years = years;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<YearSummaryDto> getYears() {
        return years;
    }

    public void setYears(List<YearSummaryDto> years) {
        this.years = years;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TrainingSummaryDto that = (TrainingSummaryDto) object;
        return isActive == that.isActive && Objects.equals(username, that.username) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, firstName, lastName, isActive);
    }
}
