//package com.epam.trainer_workload_service.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Table(name = "trainer_monthly_workload", uniqueConstraints = @UniqueConstraint(columnNames = {"username", "year", "month"}))
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@EqualsAndHashCode(of = {"username", "year", "month"})
//public class TrainerMonthlyWorkload {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String username;
//
//    @Column(name = "first_name")
//    private String firstName;
//
//    @Column(name = "last_name")
//    private String lastName;
//
//    @Column(name = "is_active")
//    private boolean active;
//
//    @Column(name = "workload_year", nullable = false)
//    private int year;
//
//    @Column(name = "workload_month", nullable = false)
//    private int month;
//
//    @Column(name = "total_minutes", nullable = false)
//    private long totalMinutes;
//
//
//    public TrainerMonthlyWorkload(String username, String firstName, String lastName, boolean active, int year, int month, long totalMinutes) {
//        this.username = username;
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.active = active;
//        this.year = year;
//        this.month = month;
//        this.totalMinutes = totalMinutes;
//    }
//}
