package com.epam.trainer_workload_service.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "trainer_monthly_workload", uniqueConstraints = @UniqueConstraint(columnNames = {"username", "year", "month"}))
public class TrainerMonthlyWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "workload_year", nullable = false)
    private int year;

    @Column(name = "workload_month", nullable = false)
    private int month;

    @Column(name = "total_minutes", nullable = false)
    private long totalMinutes;

    public TrainerMonthlyWorkload() {
    }

    public TrainerMonthlyWorkload(String username, String firstName, String lastName, boolean isActive, int year, int month, long totalMinutes) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.year = year;
        this.month = month;
        this.totalMinutes = totalMinutes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public long getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(long totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TrainerMonthlyWorkload that = (TrainerMonthlyWorkload) object;
        return isActive == that.isActive && year == that.year && month == that.month && totalMinutes == that.totalMinutes && Objects.equals(id, that.id) && Objects.equals(username, that.username) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, firstName, lastName, isActive, year, month, totalMinutes);
    }
}
