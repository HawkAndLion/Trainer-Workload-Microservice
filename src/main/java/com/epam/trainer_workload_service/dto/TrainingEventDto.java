package com.epam.trainer_workload_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingEventDto {

    private Long trainingId;
    private String username;
    private String firstName;
    private String lastName;
    private boolean active;
    private LocalDate trainingDate;
    private long durationMinutes;
    private ActionType actionType;

}
