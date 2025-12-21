package com.epam.trainer_workload_service.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(
        name = "training_event_records",
        uniqueConstraints = @UniqueConstraint(columnNames = "training_id")
)
@Getter
@Setter
@EqualsAndHashCode(of = "trainingId")
public class TrainingEventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_id", nullable = false, unique = true)
    private Long trainingId;

    @Column(nullable = false)
    private String username;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "duration_minutes", nullable = false)
    private long durationMinutes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected TrainingEventRecord() {
    }

    public TrainingEventRecord(
            Long trainingId,
            String username,
            LocalDate trainingDate,
            long durationMinutes
    ) {
        this.trainingId = trainingId;
        this.username = username;
        this.trainingDate = trainingDate;
        this.durationMinutes = durationMinutes;
        this.createdAt = LocalDateTime.now();
    }

}
